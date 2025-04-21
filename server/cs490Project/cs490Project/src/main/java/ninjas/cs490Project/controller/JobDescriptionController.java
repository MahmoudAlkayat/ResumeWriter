package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JobDescriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobDescriptionController {

    private final UserRepository userRepository;
    private final JobDescriptionService jobDescriptionService;

    public JobDescriptionController(UserRepository userRepository,
                                  JobDescriptionService jobDescriptionService) {
        this.userRepository = userRepository;
        this.jobDescriptionService = jobDescriptionService;
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
    public ResponseEntity<?> submitJobDescription(Authentication authentication,
                                                 @RequestBody JobDescriptionRequest jobDescription) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            Map<String, String> response = jobDescriptionService.submitJobDescription(user, jobDescription);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getJobDescriptions(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            List<Map<String, Object>> response = jobDescriptionService.getJobDescriptions(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}