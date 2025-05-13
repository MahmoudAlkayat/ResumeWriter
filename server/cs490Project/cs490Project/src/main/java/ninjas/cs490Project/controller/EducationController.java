package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.service.EducationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes/education")
public class EducationController {

    @Autowired
    private EducationService educationService;

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
        private String location;        // optional

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

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    // 1. GET all education for a user
    @GetMapping
    public ResponseEntity<?> getAllEducation(@AuthenticationPrincipal User user) {
        Map<String, Object> education = educationService.getAllEducation(user);
        return ResponseEntity.ok(education);
    }

    // 2. CREATE a new Education record
    @PostMapping
    public ResponseEntity<?> createEducation(@AuthenticationPrincipal User user, @RequestBody EducationRequest req) {
        educationService.createEducation(user, req);
        return ResponseEntity.ok().build();
    }

    // 3. UPDATE an Education record
    @PutMapping("/{eduId}")
    public ResponseEntity<?> updateEducation(@AuthenticationPrincipal User user, @PathVariable int eduId, @RequestBody EducationRequest req) {
        educationService.updateEducation(user, eduId, req);
        return ResponseEntity.ok().build();
    }

    // 4. DELETE an Education record
    @DeleteMapping("/{eduId}")
    public ResponseEntity<?> deleteEducation(@AuthenticationPrincipal User user, @PathVariable int eduId) {
        educationService.deleteEducation(user, eduId);
        return ResponseEntity.ok().build();
    }
}