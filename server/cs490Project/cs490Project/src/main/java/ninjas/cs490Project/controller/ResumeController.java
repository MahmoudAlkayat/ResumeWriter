package ninjas.cs490Project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.AsyncResumeParser;
import ninjas.cs490Project.service.ResumeParsingService;
import ninjas.cs490Project.service.ResumeService;
import ninjas.cs490Project.service.ResumeProcessingNotificationService;
import ninjas.cs490Project.service.ResumeGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    private final UserRepository userRepository;
    private final ResumeService resumeService;
    private final ResumeParsingService resumeParsingService;
    private final AsyncResumeParser asyncResumeParser;
    private final ResumeProcessingNotificationService notificationService;
    private final ResumeGenerationService resumeGenerationService;
    private final ObjectMapper objectMapper;

    public ResumeController(UserRepository userRepository,
                            ResumeService resumeService,
                            ResumeParsingService resumeParsingService,
                            AsyncResumeParser asyncResumeParser,
                            ResumeProcessingNotificationService notificationService,
                            ResumeGenerationService resumeGenerationService,
                            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.resumeService = resumeService;
        this.resumeParsingService = resumeParsingService;
        this.asyncResumeParser = asyncResumeParser;
        this.notificationService = notificationService;
        this.resumeGenerationService = resumeGenerationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{resumeId}/status")
    public SseEmitter subscribeToStatus(@PathVariable int resumeId) {
        return notificationService.subscribeToResumeProcessing(resumeId);
    }

    /**
     * POST /api/resumes/upload?userId={userId}
     * Uploads the file, parses it for text (Tika) and structured info (GPT),
     * saves a new Resume for the file, and separately persists any
     * Education or WorkExperience referencing the User.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          Authentication authentication) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload an empty file.");
            return ResponseEntity.badRequest().body("No file selected.");
        }
        try {
            String email = authentication.getName();
            // 1. Find the User
            User currentUser = userRepository.findByEmail(email);
            if (currentUser == null) {
                logger.error("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            // 2. Extract text via Tika and parse key information via GPT
            String extractedText = resumeParsingService.extractTextFromFile(file);

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

            // Start async processing of the resume
            asyncResumeParser.parseResume(savedResume);

            // Return success with processing status
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

    @GetMapping("/upload/history")
    public ResponseEntity<?> getUploadHistory(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        List<Resume> resumes = resumeService.getResumesByUser(currentUser);

        List<Map<String, Object>> response = new ArrayList<>();
        for (Resume resume : resumes) {
            Map<String, Object> resumeMap = new HashMap<>();
            resumeMap.put("resumeId", resume.getId());
            resumeMap.put("title", resume.getTitle() != null ? resume.getTitle().trim() : "");
            resumeMap.put("content", resume.getContent() != null ? resume.getContent().trim() : "");
            resumeMap.put("createdAt", resume.getCreatedAt());
            response.add(resumeMap);
        }

        return ResponseEntity.ok(response);
    }

    public static class GenerateResumeRequest {
        private Long jobId;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateResume(@RequestBody GenerateResumeRequest request,
                                          Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            if (request.getJobId() == null) {
                return ResponseEntity.badRequest().body("jobId is required");
            }

            // Create a new Resume entity
            Resume resume = new Resume();
            resume.setTitle("Generated Resume for Job #" + request.getJobId());
            resume.setCreatedAt(LocalDateTime.now());
            resume.setUpdatedAt(LocalDateTime.now());
            resume.setUser(currentUser);

            Resume savedResume = resumeService.storeResume(resume);
            logger.info("Created new resume with id: {}", savedResume.getId());

            // Start async processing
            new Thread(() -> {
                try {
                    ResumeParsingResult result = resumeGenerationService.generateResume(currentUser, request.getJobId());
                    
                    // Update resume content with generated result
                    resume.setContent(objectMapper.writeValueAsString(result));
                    resumeService.storeResume(resume);
                    
                    // Notify completion
                    // notificationService.notifyProcessingComplete(savedResume.getId());
                } catch (Exception e) {
                    logger.error("Error generating resume: {}", e.getMessage());
                    // notificationService.notifyProcessingError(savedResume.getId(), e.getMessage());
                }
            }).start();

            // Return the resume ID and processing status
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", savedResume.getId());
            response.put("status", "processing");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in resume generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating resume: " + e.getMessage());
        }
    }
}
