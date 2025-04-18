package ninjas.cs490Project.service;

import ninjas.cs490Project.dto.EducationData;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.dto.WorkExperienceData;
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.entity.FreeformEntry;
import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import ninjas.cs490Project.repository.FreeformEntryRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AsyncResumeParser {

    private static final Logger logger = LoggerFactory.getLogger(AsyncResumeParser.class);

    private final UploadedResumeRepository uploadedResumeRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final FreeformEntryRepository freeformEntryRepository;
    private final SkillService skillService;
    private final ProcessingStatusService processingStatusService;

    public AsyncResumeParser(UploadedResumeRepository uploadedResumeRepository,
                             WorkExperienceRepository workExperienceRepository,
                             EducationRepository educationRepository,
                             FreeformEntryRepository freeformEntryRepository,
                             SkillService skillService,
                             ProcessingStatusService processingStatusService) {
        this.uploadedResumeRepository = uploadedResumeRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.educationRepository = educationRepository;
        this.freeformEntryRepository = freeformEntryRepository;
        this.skillService = skillService;
        this.processingStatusService = processingStatusService;
    }

    @Async
    @Transactional
    public void parseResume(UploadedResume resume, ProcessingStatus status) {
        try {
            processingStatusService.startProcessing(status.getId());

            Tika tika = new Tika();
            String resumeText = tika.parseToString(new ByteArrayInputStream(resume.getFileData()));
            logger.info("Extracted resume text (first 100 chars): {}",
                    resumeText.substring(0, Math.min(resumeText.length(), 100)));

            // Parse resume details using your parsing service (e.g., GPT)
            // ResumeParsingResult parsingResult = resumeParsingService.parseKeyInformation(resumeText);
            // logger.info("Parsed result: {}", objectMapper.writeValueAsString(parsingResult));

            // TESTING
            Thread.sleep(5000);
            ResumeParsingResult parsingResult = new ResumeParsingResult();
            List<EducationData> mockEducationList = new ArrayList<>();
            EducationData mockEducation = new EducationData();
            mockEducation.setInstitution("University of Illinois at Urbana-Champaign");
            mockEducation.setDegree("Bachelor of Science");
            mockEducation.setFieldOfStudy("Computer Science");
            mockEducation.setStartDate("2021-08-15");
            mockEducation.setEndDate("2025-12-15");
            mockEducation.setGpa(3.8);
            mockEducationList.add(mockEducation);

            List<WorkExperienceData> mockWorkExperienceList = new ArrayList<>();
            WorkExperienceData mockWorkExperience = new WorkExperienceData();
            mockWorkExperience.setCompany("Google");
            mockWorkExperience.setJobTitle("Software Engineer");
            mockWorkExperience.setStartDate("2021-08-15");
            mockWorkExperience.setEndDate("2025-12-15");
            mockWorkExperience.setDescription("Developed and maintained software applications.");
            mockWorkExperienceList.add(mockWorkExperience);

            List<String> mockSkills = new ArrayList<>();
            mockSkills.add("Java");
            mockSkills.add("Python");
            mockSkills.add("C++");

            parsingResult.setEducationList(mockEducationList);
            parsingResult.setWorkExperienceList(mockWorkExperienceList);
            parsingResult.setSkills(mockSkills);

            // Update the resume content
            resume.setContent(resumeText);
            uploadedResumeRepository.save(resume);

            // Process education entries
            List<EducationData> educationList = parsingResult.getEducationList();
            if (educationList != null && !educationList.isEmpty()) {
                logger.info("Found {} education entries.", educationList.size());
                List<Education> educations = new ArrayList<>();

                for (EducationData data : educationList) {
                    Education edu = new Education();
                    edu.setInstitution(data.getInstitution());
                    edu.setDegree(data.getDegree());
                    edu.setFieldOfStudy(data.getFieldOfStudy());
                    edu.setDescription(data.getDescription());

                    // Safe date parsing
                    edu.setStartDate(
                            data.getStartDate() != null
                                    ? LocalDate.parse(data.getStartDate())
                                    : LocalDate.of(2021, 9, 1)
                    );
                    edu.setEndDate(
                            data.getEndDate() != null
                                    ? LocalDate.parse(data.getEndDate())
                                    : LocalDate.of(2025, 12, 1)
                    );

                    // Handle GPA
                    Double gpaValue = data.getGpa();
                    edu.setGpa(gpaValue);

                    edu.setUser(resume.getUser());
                    educations.add(edu);
                }
                educationRepository.saveAll(educations);
                logger.info("Saved {} education entries.", educations.size());
            }

            // Process work experience entries
            List<WorkExperienceData> workExpList = parsingResult.getWorkExperienceList();
            if (workExpList != null && !workExpList.isEmpty()) {

                for (WorkExperienceData data : workExpList) {
                    WorkExperience we = new WorkExperience();
                    we.setCompany(data.getCompany());
                    we.setJobTitle(data.getJobTitle());
                    we.setStartDate(LocalDate.parse(data.getStartDate()));

                    String endDateStr = data.getEndDate();
                    if (endDateStr == null || endDateStr.trim().isEmpty() || endDateStr.equalsIgnoreCase("N/A")) {
                        we.setEndDate(null);
                    } else {
                        we.setEndDate(LocalDate.parse(endDateStr));
                    }

                    we.setDescription(data.getDescription());
                    we.setUser(resume.getUser());
                    workExperienceRepository.save(we);
                }
                logger.info("Saved {} work experience entries.", workExpList.size());
            } else {
                logger.warn("No work experience entries found in parsed result.");
            }

            // Process skills
            List<String> skills = parsingResult.getSkills();
            if (skills != null && !skills.isEmpty()) {
                logger.info("Found {} skills to process.", skills.size());
                
                // Use SkillService to add skills with de-duplication
                List<Skill> addedSkills = skillService.addSkills(skills, resume.getUser());
                logger.info("Processed {} skills for user.", addedSkills.size());
            } else {
                logger.warn("No skills found in parsed result.");
            }

            processingStatusService.completeProcessing(status.getId());
        } catch (Exception e) {
            logger.error("Error processing resume with ID " + resume.getId(), e);
            processingStatusService.failProcessing(status.getId(), e.getMessage());
        }
    }

    @Async
    @Transactional
    public void parseFreeformCareer(String text, User user, Integer freeformId, ProcessingStatus status) {
        FreeformEntry entry = freeformEntryRepository.findById(freeformId)
                .orElseThrow(() -> new RuntimeException("FreeformEntry not found"));

        try {
            processingStatusService.startProcessing(status.getId());
            // Parse the freeform text using GPT
            // ResumeParsingResult parsingResult = resumeParsingService.parseFreeformCareer(text);

            // FOR TESTING
            Thread.sleep(5000);
            ResumeParsingResult parsingResult = new ResumeParsingResult();
            parsingResult.setWorkExperienceList(new ArrayList<>());
            WorkExperienceData mock = new WorkExperienceData();
            mock.setCompany("Company");
            mock.setJobTitle("Job Title");
            mock.setStartDate("2021-01-01");
            mock.setEndDate("2021-01-01");
            mock.setDescription("Description");
            parsingResult.getWorkExperienceList().add(mock);

            // Get existing work experience if any
            WorkExperience existingExperience = workExperienceRepository.findByFreeformEntryId(freeformId);

            // Process work experience entry
            List<WorkExperienceData> workExpList = parsingResult.getWorkExperienceList();
            if (workExpList != null && !workExpList.isEmpty()) {
                // Take only the first work experience entry
                WorkExperienceData data = workExpList.get(0);
                
                // Update existing experience or create new one
                WorkExperience we = (existingExperience != null) ? existingExperience : new WorkExperience();
                
                // Update the fields
                we.setCompany(data.getCompany());
                we.setJobTitle(data.getJobTitle());
                we.setStartDate(LocalDate.parse(data.getStartDate()));

                String endDateStr = data.getEndDate();
                if (endDateStr == null || endDateStr.trim().isEmpty() || endDateStr.equalsIgnoreCase("N/A")) {
                    we.setEndDate(null);
                } else if (endDateStr.equalsIgnoreCase("Present")) {
                    we.setEndDate(LocalDate.now());
                } else {
                    we.setEndDate(LocalDate.parse(endDateStr));
                }

                we.setDescription(data.getDescription());
                we.setUser(user);
                we.setFreeformEntry(entry);

                // Save the work experience
                workExperienceRepository.save(we);
                logger.info("Saved work experience entry from freeform text.");
                
                // Update FreeformEntry
                entry.setUpdatedAt(Instant.now());
                freeformEntryRepository.save(entry);
                
            } else {
                logger.warn("No work experience entry found in parsed freeform text.");
                entry.setUpdatedAt(Instant.now());
                freeformEntryRepository.save(entry);
            }
            processingStatusService.completeProcessing(status.getId());

        } catch (Exception e) {
            logger.error("Error parsing freeform career", e);
            entry.setUpdatedAt(Instant.now());
            freeformEntryRepository.save(entry);
            processingStatusService.failProcessing(status.getId(), e.getMessage());
        }
    }
}