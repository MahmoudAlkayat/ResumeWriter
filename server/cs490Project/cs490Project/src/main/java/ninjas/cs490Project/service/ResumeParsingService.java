package ninjas.cs490Project.service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import ninjas.cs490Project.dto.EducationData;
import ninjas.cs490Project.dto.ResumeParsingResult;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeParsingService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParsingService.class);
    private final StanfordCoreNLP pipeline;
    private final Tika tika = new Tika();

    public ResumeParsingService() {
        Properties props = new Properties();
        // Use basic annotators; disable SUTime to avoid binder issues.
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("sutime.binders", "0");
        this.pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Extracts text from an uploaded file using Apache Tika.
     *
     * @param file the uploaded resume file
     * @return the extracted text
     * @throws Exception if an error occurs during extraction
     */
    public String extractTextFromFile(MultipartFile file) throws Exception {
        logger.info("Starting text extraction from file: {}", file.getOriginalFilename());
        try {
            String text = tika.parseToString(file.getInputStream());
            logger.info("Successfully extracted text from file: {}", file.getOriginalFilename());
            return text;
        } catch (Exception e) {
            logger.error("Error extracting text from file: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }

    /**
     * Parses key information from the resume text.
     * This implementation scans the text line‐by‐line, looks for "EDUCATION" and "SKILLS" sections,
     * and uses heuristics to extract education details including dates, degree, field of study, GPA, and description.
     *
     * @param text the extracted resume text
     * @return a ResumeParsingResult object containing education records and skills.
     */
    public ResumeParsingResult parseKeyInformation(String text) {
        ResumeParsingResult result = new ResumeParsingResult();
        if (text == null || text.isBlank()) {
            return result;
        }

        // Split the text into lines
        String[] lines = text.split("\\r?\\n");
        boolean inEducation = false;
        boolean inSkills = false;
        StringBuilder eduBuilder = new StringBuilder();
        StringBuilder skillsBuilder = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("education")) {
                inEducation = true;
                inSkills = false;
                continue;
            }
            if (trimmed.equalsIgnoreCase("technical skills") || trimmed.equalsIgnoreCase("skills")) {
                inSkills = true;
                inEducation = false;
                continue;
            }
            // End sections if a new header (all uppercase, length ≥ 3) is encountered
            if (trimmed.matches("^[A-Z ]{3,}$")) {
                inEducation = false;
                inSkills = false;
            }
            if (inEducation) {
                eduBuilder.append(trimmed).append("\n");
            }
            if (inSkills) {
                skillsBuilder.append(trimmed).append(" ");
            }
        }

        String educationSection = eduBuilder.toString().trim();
        String skillsSection = skillsBuilder.toString().trim();

        // Process the education section
        if (!educationSection.isEmpty()) {
            // Assume each education entry is separated by one or more blank lines.
            String[] eduEntries = educationSection.split("\\n\\s*\\n");
            for (String entry : eduEntries) {
                EducationData eduData = extractEducationData(entry);
                result.getEducationList().add(eduData);
            }
        }

        // Process the skills section: split on commas
        if (!skillsSection.isEmpty()) {
            String cleanedSkills = skillsSection.replaceAll("(?i)programming languages:", "")
                    .replaceAll("(?i)web technologies:", "").trim();
            String[] skillArray = cleanedSkills.split(",");
            for (String skill : skillArray) {
                if (!skill.trim().isEmpty()) {
                    result.getSkills().add(skill.trim());
                }
            }
        }

        logger.info("Completed parsing resume. Education entries: {}, Skills count: {}",
                result.getEducationList().size(), result.getSkills().size());
        return result;
    }

    /**
     * Extracts education details from a block of text assumed to be one education entry.
     * The heuristic used:
     * - The **first non-empty line** is assumed to be the institution.
     * - The **second non-empty line** is used for additional details and may contain a date range.
     * - The **third non-empty line** is assumed to contain degree information. If it contains " in ",
     *   it is split into degree (left) and field of study (right).
     * - Any subsequent lines are appended to the description; if a line starts with "GPA:" the GPA is parsed.
     *
     * @param entry a block of text from the education section
     * @return an EducationData object with the extracted values
     */
    private EducationData extractEducationData(String entry) {
        EducationData edu = new EducationData();
        List<String> nonEmptyLines = Arrays.stream(entry.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (nonEmptyLines.isEmpty()) return edu;

        // 1. First line: institution
        edu.setInstitution(nonEmptyLines.get(0));

        String descriptionBuilder = "";
        // 2. Second line: additional details (e.g., date range and college name)
        if (nonEmptyLines.size() >= 2) {
            String secondLine = nonEmptyLines.get(1);
            // Extract date range pattern: e.g., "September 2021 - Expected January 2025" or "September 2021 - January 2025"
            Pattern rangePattern = Pattern.compile("([A-Za-z]+\\s+\\d{4})\\s*-\\s*(?:Expected\\s+)?([A-Za-z]+\\s+\\d{4})");
            Matcher rangeMatcher = rangePattern.matcher(secondLine);
            if (rangeMatcher.find()) {
                String startStr = rangeMatcher.group(1);
                String endStr = rangeMatcher.group(2);
                edu.setStartDate(parseDate(startStr));
                edu.setEndDate(parseDate(endStr));
                // Remove the date range from secondLine for description purposes.
                secondLine = secondLine.replace(rangeMatcher.group(0), "").trim();
            }
            descriptionBuilder += secondLine;
        }

        // 3. Third line: degree info (degree and field of study)
        if (nonEmptyLines.size() >= 3) {
            String thirdLine = nonEmptyLines.get(2);
            if (thirdLine.contains(" in ")) {
                String[] parts = thirdLine.split(" in ", 2);
                edu.setDegree(parts[0].trim());
                edu.setFieldOfStudy(parts[1].trim());
            } else {
                edu.setDegree(thirdLine.trim());
            }
        }

        // 4. Process remaining lines: append to description and extract GPA if present.
        for (int i = 3; i < nonEmptyLines.size(); i++) {
            String line = nonEmptyLines.get(i);
            if (line.toLowerCase().startsWith("gpa:")) {
                Pattern gpaPattern = Pattern.compile("(?i)gpa:\\s*([0-9\\.]+)");
                Matcher gpaMatcher = gpaPattern.matcher(line);
                if (gpaMatcher.find()) {
                    try {
                        edu.setGpa(Double.parseDouble(gpaMatcher.group(1)));
                    } catch (NumberFormatException ex) {
                        logger.warn("Unable to parse GPA from line: {}", line);
                    }
                }
            } else {
                descriptionBuilder += " " + line;
            }
        }
        edu.setDescription(descriptionBuilder.trim());
        return edu;
    }

    /**
     * Helper method to parse a date string in the format "Month YYYY" into a LocalDate.
     * Assumes day 1.
     *
     * @param dateStr the date string (e.g., "September 2021")
     * @return the corresponding LocalDate or null if parsing fails
     */
    private LocalDate parseDate(String dateStr) {
        dateStr = dateStr.trim();
        String[] parts = dateStr.split("\\s+");
        if (parts.length < 2) {
            return null;
        }
        int month = parseMonth(parts[0]);
        int year;
        try {
            year = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        return LocalDate.of(year, month, 1);
    }

    /**
     * Helper method to convert a month name (or abbreviation) into its numeric value.
     *
     * @param monthStr the month name (e.g., "Sep" or "September")
     * @return the numeric month value (1-12)
     */
    private int parseMonth(String monthStr) {
        monthStr = monthStr.toLowerCase();
        switch (monthStr) {
            case "january":
            case "jan":
                return 1;
            case "february":
            case "feb":
                return 2;
            case "march":
            case "mar":
                return 3;
            case "april":
            case "apr":
                return 4;
            case "may":
                return 5;
            case "june":
            case "jun":
                return 6;
            case "july":
            case "jul":
                return 7;
            case "august":
            case "aug":
                return 8;
            case "september":
            case "sep":
            case "sept":
                return 9;
            case "october":
            case "oct":
                return 10;
            case "november":
            case "nov":
                return 11;
            case "december":
            case "dec":
                return 12;
            default:
                return 1; // fallback to January
        }
    }
}
