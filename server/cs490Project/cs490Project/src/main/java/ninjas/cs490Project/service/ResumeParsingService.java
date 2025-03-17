package ninjas.cs490Project.service;

import ninjas.cs490Project.dto.ResumeData;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ResumeParsingService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParsingService.class);
    private final Tika tika = new Tika();

    /**
     * Extracts text from an uploaded file using Apache Tika.
     * @param file the uploaded resume file
     * @return the extracted text
     * @throws Exception if an error occurs during parsing
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
     * Optionally, parse key information from the resume text.
     * For example, you can use regular expressions or NLP libraries to extract sections like Skills, Education, etc.
     * @param text the extracted resume text
     * @return a simple summary or an object containing parsed data
     */
    public ResumeData parseKeyInformation(String text) {
        int textLength = text != null ? text.length() : 0;
        logger.info("Parsing key information from resume text of length: {}", textLength);
        ResumeData data = new ResumeData();

        if (text != null && text.toLowerCase().contains("skills")) {
            logger.debug("Found 'skills' in resume text. Extracting skills.");
            data.setSkills(Arrays.asList("Java", "Spring Boot", "SQL"));
        }
        if (text != null && text.toLowerCase().contains("education")) {
            logger.debug("Found 'education' in resume text. Extracting education details.");
            data.setEducation("Extracted education here");
        }
        if (text != null && text.toLowerCase().contains("experience")) {
            logger.debug("Found 'experience' in resume text. Extracting work history details.");
            data.setWorkHistory("Extracted work history details here");
        }
        logger.info("Completed parsing key information from resume text");
        logger.debug("Parsed resume data: {}", data);
        return data;
    }
}
