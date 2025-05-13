package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.service.JobAdviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private JobAdviceService jobAdviceService;

    public static class JobAdviceRequest {
        private Long jobId;
        private Long resumeId;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public Long getResumeId() {
            return resumeId;
        }

        public void setResumeId(Long resumeId) {
            this.resumeId = resumeId;
        }
    }

    @PostMapping("/advice")
    public ResponseEntity<?> generateJobAdvice(@AuthenticationPrincipal User user, @RequestBody JobAdviceRequest request) {
        try {
            if (request.getJobId() == null || request.getResumeId() == null) {
                return ResponseEntity.badRequest()
                        .body("Both jobId and resumeId are required");
            }

            // Verify user has access to both the job description and resume
            JobDescription jobDescription = jobDescriptionRepository.findById(request.getJobId())
                    .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
            
            if (user.getId() != jobDescription.getUser().getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to access this job description");
            }

            // Generate advice
            String advice = jobAdviceService.generateAdvice(request.getJobId(), request.getResumeId());

            Map<String, String> response = new HashMap<>();
            response.put("advice", advice);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 