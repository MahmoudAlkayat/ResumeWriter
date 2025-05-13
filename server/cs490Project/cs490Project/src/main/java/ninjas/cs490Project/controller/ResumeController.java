package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.service.AsyncResumeParser;
import ninjas.cs490Project.service.ResumeParsingService;
import ninjas.cs490Project.service.ResumeGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ninjas.cs490Project.service.ResumeService;
import ninjas.cs490Project.service.ProcessingStatusService;
import ninjas.cs490Project.service.ResumeFormattingService;
import ninjas.cs490Project.entity.FormattedResume;
import ninjas.cs490Project.repository.FormattedResumeRepository;
import ninjas.cs490Project.service.ResumeTemplateService;
import ninjas.cs490Project.entity.ResumeTemplate;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    private final JobDescriptionRepository jobDescriptionRepository;
    private final UploadedResumeRepository uploadedResumeRepository;
    private final ResumeParsingService resumeParsingService;
    private final ResumeGenerationService resumeGenerationService;
    private final AsyncResumeParser asyncResumeParser;
    private final ResumeService resumeService;
    private final ProcessingStatusService processingStatusService;
    private final ResumeFormattingService resumeFormattingService;
    private final FormattedResumeRepository formattedResumeRepository;
    private final ResumeTemplateService templateService;

    public ResumeController(UserRepository userRepository,
                          JobDescriptionRepository jobDescriptionRepository,
                          UploadedResumeRepository uploadedResumeRepository,
                          ResumeParsingService resumeParsingService,
                          ResumeGenerationService resumeGenerationService,
                          AsyncResumeParser asyncResumeParser,
                          ResumeService resumeService,
                          ProcessingStatusService processingStatusService,
                          ResumeFormattingService resumeFormattingService,
                          FormattedResumeRepository formattedResumeRepository,
                          ResumeTemplateService templateService) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.uploadedResumeRepository = uploadedResumeRepository;
        this.resumeParsingService = resumeParsingService;
        this.resumeGenerationService = resumeGenerationService;
        this.asyncResumeParser = asyncResumeParser;
        this.resumeService = resumeService;
        this.processingStatusService = processingStatusService;
        this.resumeFormattingService = resumeFormattingService;
        this.formattedResumeRepository = formattedResumeRepository;
        this.templateService = templateService;
    }

    /**
     * POST /api/resumes/upload?userId={userId}
     * Uploads the file, parses it for text (Tika) and structured info (GPT),
     * saves a new Resume for the file, and separately persists any
     * Education or WorkExperience referencing the User.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload an empty file.");
            return ResponseEntity.badRequest().body("No file selected.");
        }
        try {
            // Extract text via Tika and parse key information via GPT
            String extractedText = resumeParsingService.extractTextFromFile(file);

            // Create and save a new UploadedResume entity
            UploadedResume resume = new UploadedResume();
            resume.setTitle(file.getOriginalFilename());
            resume.setContent(extractedText);
            resume.setFileData(file.getBytes());
            resume.setCreatedAt(Instant.now());
            resume.setUpdatedAt(Instant.now());
            resume.setUser(user);

            UploadedResume savedResume = uploadedResumeRepository.save(resume);
            logger.info("Resume uploaded and stored successfully with id: {}", savedResume.getId());

            // Create processing status
            ProcessingStatus status = processingStatusService.createProcessingStatus(
                user,
                ProcessingStatus.ProcessingType.UPLOADED_RESUME,
                savedResume.getId()
            );
            
            processingStatusService.startProcessing(status.getId());
            asyncResumeParser.parseResume(savedResume, status);

            // Return success with processing status
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", savedResume.getId());
            response.put("statusId", status.getId());
            response.put("status", "processing");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading resume: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading resume: " + e.getMessage());
        }
    }

    @GetMapping("/upload/history")
    public ResponseEntity<?> getUploadHistory(@AuthenticationPrincipal User user) {
        List<UploadedResume> resumes = uploadedResumeRepository.findByUser(user);

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

    @GetMapping("/generate/history")
    public ResponseEntity<?> getGeneratedHistory(@AuthenticationPrincipal User user) {
        List<GeneratedResume> resumes = resumeService.getGeneratedResumesByUser(user);

        List<Map<String, Object>> response = new ArrayList<>();
        for (GeneratedResume resume : resumes) {
            Map<String, Object> resumeMap = new HashMap<>();
            resumeMap.put("resumeId", resume.getId());
            resumeMap.put("content", resume.getContent() != null ? resume.getContent().trim() : "");
            resumeMap.put("createdAt", resume.getCreatedAt());
            resumeMap.put("updatedAt", resume.getUpdatedAt());
            resumeMap.put("jobId", resume.getJobDescription().getId());
            resumeMap.put("jobDescriptionTitle", resume.getJobDescription().getJobTitle());
            resumeMap.put("resumeTitle", resume.getTitle() != null ? resume.getTitle() : "");
            response.add(resumeMap);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/upload/{resumeId}/original")
    public ResponseEntity<?> getOriginalFile(@PathVariable Long resumeId, @AuthenticationPrincipal User user) {
        try {
            UploadedResume resume = uploadedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

            // Check if the current user owns this resume
            if (resume.getUser().getId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to access this file");
            }

            // Set up response headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", resume.getTitle());
            headers.setContentLength(resume.getFileData().length);

            return new ResponseEntity<>(resume.getFileData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving original file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving file: " + e.getMessage());
        }
    }

    public static class GenerateResumeRequest {
        private Long jobId;
        private String title;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateResume(@AuthenticationPrincipal User user, @RequestBody GenerateResumeRequest request) {
        try {
            if (request.getJobId() == null) {
                return ResponseEntity.badRequest().body("jobId is required");
            }

            JobDescription jobDescription = jobDescriptionRepository.findById(request.getJobId())
                    .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

            if (user.getId() != jobDescription.getUser().getId()) {
                return ResponseEntity.badRequest().body("User does not have access to this job description");
            }

            // Create a new Resume entity
            GeneratedResume resume = new GeneratedResume();
            resume.setCreatedAt(Instant.now());
            resume.setUpdatedAt(Instant.now());
            resume.setJobDescription(jobDescription);
            resume.setUser(user);
            resume.setTitle(request.getTitle());

            GeneratedResume savedResume = resumeService.storeGeneratedResume(resume);
            logger.info("Created new resume with id: {}", savedResume.getId());

            // Create processing status
            ProcessingStatus status = processingStatusService.createProcessingStatus(
                user,
                ProcessingStatus.ProcessingType.GENERATED_RESUME,
                savedResume.getId()
            );
            
            resumeGenerationService.generateResume(user, request.getJobId(), savedResume, status);
            
            // Return the resume ID and processing status
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", savedResume.getId());
            response.put("statusId", status.getId());
            response.put("status", "processing");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in resume generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating resume: " + e.getMessage());
        }
    }

    public static class FormatResumeRequest {
        private Long resumeId;
        private String formatType;
        private String templateId;

        public Long getResumeId() {
            return resumeId;
        }

        public void setResumeId(Long resumeId) {
            this.resumeId = resumeId;
        }

        public String getFormatType() {
            return formatType;
        }

        public void setFormatType(String formatType) {
            this.formatType = formatType;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }
    }

    @PostMapping("/format")
    public ResponseEntity<?> formatResume(@AuthenticationPrincipal User user, @RequestBody FormatResumeRequest request) {
        try {
            if (request.getResumeId() == null) {
                return ResponseEntity.badRequest().body("resumeId is required");
            }

            GeneratedResume generatedResume = resumeService.getGeneratedResumeById(request.getResumeId());
            if (generatedResume == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Resume not found");
            }

            if (user.getId() != generatedResume.getUser().getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to access this resume");
            }

            String formatType = request.getFormatType() != null ? request.getFormatType() : "markdown";
            FormattedResume formattedResume = resumeFormattingService.formatResume(
                generatedResume,
                formatType,
                user,
                request.getTemplateId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("formattedResumeId", formattedResume.getId());
            response.put("formatType", formattedResume.getFormatType());
            response.put("fileExtension", formattedResume.getFileExtension());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid format type: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error formatting resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error formatting resume: " + e.getMessage());
        }
    }

    @GetMapping("/download/{formattedResumeId}")
    public ResponseEntity<?> downloadFormattedResume(@AuthenticationPrincipal User user, @PathVariable Long formattedResumeId) {
        try {
            FormattedResume formattedResume = formattedResumeRepository.findById(formattedResumeId)
                    .orElseThrow(() -> new IllegalArgumentException("Formatted resume not found"));

            if (user.getId() != formattedResume.getUser().getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to access this file");
            }

            // Set up response headers for file download
            HttpHeaders headers = new HttpHeaders();
            String contentType;
            switch (formattedResume.getFormatType().toLowerCase()) {
                case "markdown":
                    contentType = "text/markdown";
                    break;
                case "html":
                    contentType = "text/html";
                    break;
                case "text":
                    contentType = "text/plain";
                    break;
                case "pdf":
                    contentType = "application/pdf";
                    break;
                default:
                    contentType = "application/octet-stream";
            }
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", 
                "resume." + formattedResume.getFileExtension());

            // For LaTeX format, return the PDF content
            if (formattedResume.getFormatType().toLowerCase().equals("pdf")) {
                return new ResponseEntity<>(formattedResume.getPdfContent(), headers, HttpStatus.OK);
            }

            return new ResponseEntity<>(formattedResume.getContent(), headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error downloading resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error downloading resume: " + e.getMessage());
        }
    }

    @DeleteMapping("/generate/{resumeId}")
    public ResponseEntity<?> deleteGeneratedResume(@AuthenticationPrincipal User user, @PathVariable Long resumeId) {
        try {
            GeneratedResume resume = resumeService.getGeneratedResumeById(resumeId);
            if (resume == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Resume not found");
            }

            // Check if the current user owns this resume
            if (resume.getUser().getId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to delete this resume");
            }

            resumeService.deleteGeneratedResume(resumeId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error deleting resume: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting resume: " + e.getMessage());
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates() {
        try {
            List<ResumeTemplate> templates = templateService.getAllTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            logger.error("Error retrieving templates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving templates: " + e.getMessage());
        }
    }
}