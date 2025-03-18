package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.EducationData;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.SkillRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.ResumeParsingService;
import ninjas.cs490Project.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeParsingService resumeParsingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          @RequestParam("userId") int userId) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload an empty file.");
            return ResponseEntity.badRequest().body("No file selected.");
        }
        try {
            // Retrieve user from DB
            User currentUser = userRepository.findUserById(userId);
            if (currentUser == null) {
                logger.error("User not found with id: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id: " + userId);
            }

            // Extract text from the uploaded file using Tika via our parser
            String extractedText = resumeParsingService.extractTextFromFile(file);

            // Parse key information (education, skills, etc.) from the extracted text
            ResumeParsingResult parsedResult = resumeParsingService.parseKeyInformation(extractedText);

            // Create and populate a new Resume entity
            Resume resume = new Resume();
            resume.setTitle(file.getOriginalFilename());
            resume.setContent(extractedText);
            resume.setFileContent(file.getBytes());
            resume.setCreatedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());
            resume.setUser(currentUser);

            // Create Education entities from parsedResult.educationList
            Set<Education> educationSet = new HashSet<>();
            for (EducationData eduData : parsedResult.getEducationList()) {
                Education edu = new Education();
                edu.setInstitution(eduData.getInstitution());
                edu.setDegree(eduData.getDegree());
                edu.setFieldOfStudy(eduData.getFieldOfStudy());
                edu.setDescription(eduData.getDescription());
                // Use the parsed start/end dates if available; otherwise you could set defaults
                edu.setStartDate(eduData.getStartDate() != null ? eduData.getStartDate() : LocalDate.of(2021, 9, 1));
                edu.setEndDate(eduData.getEndDate() != null ? eduData.getEndDate() : LocalDate.of(2025, 12, 1));
                edu.setGpa(eduData.getGpa()); // Set GPA if available (can be null)
                edu.setResume(resume);
                educationSet.add(edu);
            }
            resume.setEducationRecords(educationSet);

            // Create Skill entities from parsedResult.skills
            Set<Skill> skillSet = new HashSet<>();
            for (String skillName : parsedResult.getSkills()) {
                // Optionally check if the skill already exists in the DB
                Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(skillName);
                if (existingSkill.isPresent()) {
                    skillSet.add(existingSkill.get());
                } else {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillName);
                    // Save new skill to DB
                    newSkill = skillRepository.save(newSkill);
                    skillSet.add(newSkill);
                }
            }
            resume.setSkills(skillSet);

            // Save the resume (with cascade, the Education records will be saved automatically)
            Resume savedResume = resumeService.storeResume(resume);
            logger.info("Resume uploaded and stored successfully with id: {}", savedResume.getId());
            return ResponseEntity.ok("Resume uploaded successfully with id: " + savedResume.getId());
        } catch (Exception e) {
            logger.error("Error uploading resume: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading resume: " + e.getMessage());
        }
    }
}
