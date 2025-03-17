package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint to upload a resume. Expects a file and a userId as request parameters.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          @RequestParam("userId") int userId) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload an empty file.");
            return ResponseEntity.badRequest().body("No file selected.");
        }
        try {
            // Convert file bytes to a String (assuming text content for now)
            String content = new String(file.getBytes());

            // Retrieve user using the custom findUserById method in UserRepository
            User currentUser = userRepository.findUserById(userId);
            if (currentUser == null) {
                logger.error("User not found with id: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id: " + userId);
            }

            // Create and populate a new Resume entity
            Resume resume = new Resume();
            resume.setTitle(file.getOriginalFilename());
            resume.setContent(content);
            resume.setCreatedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());
            resume.setUser(currentUser); // Associate the resume with the retrieved user

            // Save the resume to the database
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
