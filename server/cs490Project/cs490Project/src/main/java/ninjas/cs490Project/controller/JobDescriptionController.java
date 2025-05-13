package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JobDescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;

    public JobDescriptionController(UserRepository userRepository,
                                  JobDescriptionService jobDescriptionService) {
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
    public ResponseEntity<?> submitJobDescription(@AuthenticationPrincipal User user, @RequestBody JobDescriptionRequest jobDescription) {
        Map<String, String> response = jobDescriptionService.submitJobDescription(user, jobDescription);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getJobDescriptions(@AuthenticationPrincipal User user) {
        try {
            List<Map<String, Object>> response = jobDescriptionService.getJobDescriptions(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}