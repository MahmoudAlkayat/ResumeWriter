package ninjas.cs490Project.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.entity.User;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/jobs")
public class JobDescriptionController {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;

    public JobDescriptionController(JobDescriptionRepository jobDescriptionRepository, UserRepository userRepository) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.userRepository = userRepository;
    }

    public static class JobDescriptionRequest {
        private String jobTitle;
        private String jobDescription;

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public void setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitJobDescription(Authentication authentication, @RequestBody JobDescriptionRequest jobDescription) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (jobDescription.getJobTitle() != null && jobDescription.getJobTitle().length() > 255) {
            return ResponseEntity.badRequest().body("Job title must not exceed 255 characters");
        }

        JobDescription job = new JobDescription();
        job.setUser(user);
        job.setJobTitle(jobDescription.getJobTitle());
        job.setJobDescription(jobDescription.getJobDescription());
        job.setCreatedAt(Instant.now());
        jobDescriptionRepository.save(job);

        Map<String, String> response = new HashMap<>();
        response.put("jobId", String.valueOf(job.getId()));
        response.put("status", "saved");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getJobDescriptions(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        List<JobDescription> jobDescriptions = jobDescriptionRepository.findByUserId(Long.valueOf(user.getId()));

        List<Map<String, Object>> response = new ArrayList<>();
        for (JobDescription job : jobDescriptions) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("jobId", job.getId());
            jobMap.put("title", job.getJobTitle());
            jobMap.put("text", job.getJobDescription());
            jobMap.put("submittedAt", job.getCreatedAt());
            response.add(jobMap);
        }
        return ResponseEntity.ok(response);
    }
}
