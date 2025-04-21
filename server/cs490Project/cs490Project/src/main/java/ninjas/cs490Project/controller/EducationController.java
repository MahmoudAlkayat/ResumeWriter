package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.EducationService;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes/education")
public class EducationController {

    private final UserRepository userRepository;
    private final EducationService educationService;

    public EducationController(UserRepository userRepository,
                             EducationService educationService) {
        this.userRepository = userRepository;
        this.educationService = educationService;
    }

    // ------------------------------
    // Data Transfer Object (DTO)
    // ------------------------------
    public static class EducationRequest {
        private String institution;
        private String degree;
        private String fieldOfStudy;    // optional
        private String description;     // optional
        private String startDate;
        private String endDate;
        private Double gpa;

        // Getters and setters
        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }

        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }

        public String getFieldOfStudy() { return fieldOfStudy; }
        public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public Double getGpa() { return gpa; }
        public void setGpa(Double gpa) { this.gpa = gpa; }
    }

    // 1. GET all education for a user
    @GetMapping
    public ResponseEntity<?> getAllEducation(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.ok(Map.of("education", List.of()));
        }

        try {
            return ResponseEntity.ok(educationService.getAllEducation(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. CREATE a new Education record
    @PostMapping
    public ResponseEntity<?> createEducation(Authentication authentication,
                                           @RequestBody EducationRequest req) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            educationService.createEducation(user, req);
            return ResponseEntity.ok("Created new education record");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. UPDATE an Education record
    @PutMapping("/{eduId}")
    public ResponseEntity<?> updateEducation(Authentication authentication,
                                           @PathVariable("eduId") int eduId,
                                           @RequestBody EducationRequest req) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            educationService.updateEducation(user, eduId, req);
            return ResponseEntity.ok("Updated education record");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. DELETE an Education record
    @DeleteMapping("/{eduId}")
    public ResponseEntity<?> deleteEducation(Authentication authentication,
                                           @PathVariable("eduId") int eduId) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            educationService.deleteEducation(user, eduId);
            return ResponseEntity.ok("Deleted education record");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}