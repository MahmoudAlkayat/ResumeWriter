package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/user/job-applications")
public class JobApplicationController {
    @Autowired
    private JobApplicationService jobApplicationService;

    @PostMapping
    public ResponseEntity<?> recordApplication(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> request) {
        try {
            String resumeId = request.get("resumeId");
            String jobId = request.get("jobId");

            if (resumeId == null || jobId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Both resumeId and jobId are required"
                ));
            }

            Map<String, Object> response = jobApplicationService.recordApplication(
                user,
                Long.parseLong(resumeId),
                Long.parseLong(jobId)
            );
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid ID format"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "An unexpected error occurred"
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getApplicationHistory(@AuthenticationPrincipal User user) {
        try {
            List<Map<String, Object>> applications = jobApplicationService.getApplicationHistory(user);
            return ResponseEntity.ok(Map.of("applications", applications));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "An unexpected error occurred"
            ));
        }
    }
} 