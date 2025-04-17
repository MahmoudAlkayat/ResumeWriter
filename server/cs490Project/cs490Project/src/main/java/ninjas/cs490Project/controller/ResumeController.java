package ninjas.cs490Project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import ninjas.cs490Project.service.AsyncResumeParser;
import ninjas.cs490Project.service.ResumeParsingService;
import ninjas.cs490Project.service.ResumeGenerationService;
import ninjas.cs490Project.service.ResumeProcessingNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.multipart.MultipartFile;
import ninjas.cs490Project.service.ResumeService;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    private final UserRepository userRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final UploadedResumeRepository uploadedResumeRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final ResumeParsingService resumeParsingService;
    private final ResumeGenerationService resumeGenerationService;
    private final AsyncResumeParser asyncResumeParser;
    private final ResumeProcessingNotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final ResumeService resumeService;

    public ResumeController(UserRepository userRepository,
                          JobDescriptionRepository jobDescriptionRepository,
                          UploadedResumeRepository uploadedResumeRepository,
                          GeneratedResumeRepository generatedResumeRepository,
                          ResumeParsingService resumeParsingService,
                          ResumeGenerationService resumeGenerationService,
                          AsyncResumeParser asyncResumeParser,
                          ResumeProcessingNotificationService notificationService,
                          ObjectMapper objectMapper,
                          ResumeService resumeService) {
        this.userRepository = userRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.uploadedResumeRepository = uploadedResumeRepository;
        this.generatedResumeRepository = generatedResumeRepository;
        this.resumeParsingService = resumeParsingService;
        this.resumeGenerationService = resumeGenerationService;
        this.asyncResumeParser = asyncResumeParser;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.resumeService = resumeService;
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
            User currentUser = userRepository.findByEmail(email);
            if (currentUser == null) {
                logger.error("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            // Extract text via Tika and parse key information via GPT
            String extractedText = resumeParsingService.extractTextFromFile(file);

            // Create and save a new UploadedResume entity
            UploadedResume resume = new UploadedResume();
            resume.setTitle(file.getOriginalFilename());
            resume.setContent(extractedText);
            resume.setFileData(file.getBytes());
            resume.setCreatedAt(Instant.now());
            resume.setUpdatedAt(Instant.now());
            resume.setUser(currentUser);

            UploadedResume savedResume = uploadedResumeRepository.save(resume);
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

        List<UploadedResume> resumes = uploadedResumeRepository.findByUser(currentUser);

        List<Map<String, Object>> response = new ArrayList<>();
        for (UploadedResume resume : resumes) {
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

            JobDescription jobDescription = jobDescriptionRepository.findById(request.getJobId())
                    .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

            // Create a new Resume entity
            GeneratedResume resume = new GeneratedResume();
            resume.setCreatedAt(Instant.now());
            resume.setUpdatedAt(Instant.now());
            resume.setJobDescription(jobDescription);
            resume.setUser(currentUser);

            resumeService.storeGeneratedResume(resume);
            logger.info("Created new resume with id: {}", resume.getId());

            // Start async processing
            new Thread(() -> {
                try {
                    ResumeParsingResult result = resumeGenerationService.generateResumeTest(currentUser, request.getJobId());
                    
                    // Update resume content with generated result
                    resume.setContent(objectMapper.writeValueAsString(result));
                    resume.setUpdatedAt(Instant.now());
                    resumeService.storeGeneratedResume(resume);
                    
                    // Notify completion
                    notificationService.notifyProcessingComplete(Integer.valueOf(resume.getId().toString()));
                } catch (Exception e) {
                    logger.error("Error generating resume: {}", e.getMessage());
                    // notificationService.notifyProcessingError(savedResume.getId(), e.getMessage());
                }
            }).start();

            // Return the resume ID and processing status
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", resume.getId());
            response.put("status", "processing");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in resume generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating resume: " + e.getMessage());
        }
    }
}
