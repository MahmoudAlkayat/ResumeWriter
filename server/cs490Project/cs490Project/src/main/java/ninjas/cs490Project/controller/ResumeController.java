package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.EducationData;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.dto.WorkExperienceData;
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.SkillRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import ninjas.cs490Project.service.AsyncResumeParser;
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
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ResumeService resumeService;
    private final ResumeParsingService resumeParsingService;

    // Added repositories to save Education & WorkExperience directly to the user
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository workExperienceRepository;

    private final AsyncResumeParser asyncResumeParser;

    public ResumeController(UserRepository userRepository,
                            SkillRepository skillRepository,
                            ResumeService resumeService,
                            ResumeParsingService resumeParsingService,
                            EducationRepository educationRepository,
                            WorkExperienceRepository workExperienceRepository,
                            AsyncResumeParser asyncResumeParser) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.resumeService = resumeService;
        this.resumeParsingService = resumeParsingService;
        this.educationRepository = educationRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.asyncResumeParser = asyncResumeParser;
    }

    /**
     * POST /api/resumes/upload?userId={userId}
     * Uploads the file, parses it for text (Tika) and structured info (GPT),
     * saves a new Resume for the file, and separately persists any
     * Education or WorkExperience referencing the User.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          @RequestParam("userId") int userId) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload an empty file.");
            return ResponseEntity.badRequest().body("No file selected.");
        }
        try {
            // 1. Find the User
            User currentUser = userRepository.findUserById(userId);
            if (currentUser == null) {
                logger.error("User not found with id: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id: " + userId);
            }

            // 2. Extract text via Tika and parse key information via GPT
            String extractedText = resumeParsingService.extractTextFromFile(file);
            ResumeParsingResult parsedResult = resumeParsingService.parseKeyInformation(extractedText);

            // 3. Create and save a new Resume entity (for the file data)
            Resume resume = new Resume();
            resume.setTitle(file.getOriginalFilename());
            resume.setContent(extractedText);
            resume.setFileData(file.getBytes());
            resume.setCreatedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());
            resume.setUser(currentUser);

            Resume savedResume = resumeService.storeResume(resume);
            logger.info("Resume uploaded and stored successfully with id: {}", savedResume.getId());

            asyncResumeParser.parseResume(savedResume);

            // 4. Create Education entries from the parsing result, tied to the User
            for (EducationData eduData : parsedResult.getEducationList()) {
                Education edu = new Education();
                edu.setInstitution(eduData.getInstitution());
                edu.setDegree(eduData.getDegree());
                edu.setFieldOfStudy(eduData.getFieldOfStudy());
                edu.setDescription(eduData.getDescription());

                // Safe date parsing, fallback used if null
                edu.setStartDate(
                        eduData.getStartDate() != null
                                ? LocalDate.parse(eduData.getStartDate())
                                : LocalDate.of(2021, 9, 1)
                );
                edu.setEndDate(
                        eduData.getEndDate() != null
                                ? LocalDate.parse(eduData.getEndDate())
                                : LocalDate.of(2025, 12, 1)
                );

                // Correctly handle the Double type by defaulting null to 0.0
                Double gpaValue = eduData.getGpa();
                if (gpaValue == null) {
                    gpaValue = 0.0;
                }
                edu.setGpa(gpaValue);

                // Instead of setResume(...), we link the user
                edu.setUser(currentUser);

                educationRepository.save(edu);
            }

            // (Optionally) create WorkExperience entries similarly,
            // linking them to currentUser, not resume.

            // Return success
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
