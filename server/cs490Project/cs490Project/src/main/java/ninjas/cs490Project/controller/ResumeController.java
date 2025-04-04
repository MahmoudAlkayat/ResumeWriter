package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.EducationData;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.dto.WorkExperienceData; // NEW import for work experience DTO
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.WorkExperience; // NEW import for work experience entity
import ninjas.cs490Project.repository.SkillRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.ResumeParsingService;
import ninjas.cs490Project.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ResumeService resumeService;
    private final ResumeParsingService resumeParsingService;

    public ResumeController(UserRepository userRepository,
                            SkillRepository skillRepository,
                            ResumeService resumeService,
                            ResumeParsingService resumeParsingService) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.resumeService = resumeService;
        this.resumeParsingService = resumeParsingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          @RequestParam("userId") int userId) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload an empty file.");
            return ResponseEntity.badRequest().body("No file selected.");
        }
        try {
            // Retrieve user from the database by userId
            User currentUser = userRepository.findUserById(userId);
            if (currentUser == null) {
                logger.error("User not found with id: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id: " + userId);
            }

            // Extract text from the uploaded file using Tika via the parsing service
            String extractedText = resumeParsingService.extractTextFromFile(file);

            // Parse key information (such as education, skills, and work experience) from the extracted text using GPT (or rules)
            ResumeParsingResult parsedResult = resumeParsingService.parseKeyInformation(extractedText);

            // Create and populate a new Resume entity
            Resume resume = new Resume();
            resume.setTitle(file.getOriginalFilename());
            resume.setContent(extractedText);
            resume.setFileData(file.getBytes());
            resume.setCreatedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());
            resume.setUser(currentUser);

            // Create Education entities from the parsed education list
            Set<Education> educationSet = new HashSet<>();
            for (EducationData eduData : parsedResult.getEducationList()) {
                Education edu = new Education();
                edu.setInstitution(eduData.getInstitution());
                edu.setDegree(eduData.getDegree());
                edu.setFieldOfStudy(eduData.getFieldOfStudy());
                edu.setDescription(eduData.getDescription());
                edu.setStartDate(eduData.getStartDate() != null
                        ? LocalDate.parse(eduData.getStartDate())
                        : LocalDate.of(2021, 9, 1));
                edu.setEndDate(eduData.getEndDate() != null
                        ? LocalDate.parse(eduData.getEndDate())
                        : LocalDate.of(2025, 12, 1));
                edu.setGpa(eduData.getGpa());
                edu.setResume(resume);
                educationSet.add(edu);
            }
            resume.setEducationRecords(educationSet);

            // Create Skill entities from the parsed skills
            Set<Skill> skillSet = new HashSet<>();
            for (String skillName : parsedResult.getSkills()) {
                Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(skillName);
                if (existingSkill.isPresent()) {
                    skillSet.add(existingSkill.get());
                } else {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillName);
                    newSkill = skillRepository.save(newSkill);
                    skillSet.add(newSkill);
                }
            }
            resume.setSkills(skillSet);

            // Create WorkExperience entities from the parsed work experience list
            if (parsedResult.getWorkExperienceList() != null) {
                Set<WorkExperience> workExperienceSet = new HashSet<>();
                for (WorkExperienceData workData : parsedResult.getWorkExperienceList()) {
                    WorkExperience workExp = new WorkExperience();
                    workExp.setCompany(workData.getCompany());
                    workExp.setJobTitle(workData.getJobTitle());
                    workExp.setStartDate(workData.getStartDate() != null
                            ? LocalDate.parse(workData.getStartDate())
                            : LocalDate.of(2000, 1, 1));
                    // If endDate is "N/A" or null, we assume it's a current job, so set to null
                    if (workData.getEndDate() != null && !"N/A".equals(workData.getEndDate())) {
                        workExp.setEndDate(LocalDate.parse(workData.getEndDate()));
                    } else {
                        workExp.setEndDate(null);
                    }
                    workExp.setDescription(workData.getDescription());
                    workExp.setResume(resume);
                    workExperienceSet.add(workExp);
                }
                resume.setWorkExperiences(workExperienceSet);
            }

            // Save the resume via the ResumeService (which handles persistence)
            Resume savedResume = resumeService.storeResume(resume);
            logger.info("Resume uploaded and stored successfully with id: {}", savedResume.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", savedResume.getId());
            response.put("status", "processing");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error uploading resume: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading resume: " + e.getMessage());
        }
    }
}
