package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/users/{userId}/education")
public class EducationController {

    private final EducationRepository educationRepository;
    private final UserRepository userRepository;

    public EducationController(EducationRepository educationRepository,
                               UserRepository userRepository) {
        this.educationRepository = educationRepository;
        this.userRepository = userRepository;
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
    public ResponseEntity<?> getAllEducation(@PathVariable("userId") int userId) {
        // Ensure user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.ok(Collections.singletonMap("education", new ArrayList<>()));
        }

        // Find all Education entries for this user
        List<Education> educationList = educationRepository.findByUserId(userId);

        // Convert to a DTO-like format
        List<Map<String, Object>> eduDtoList = new ArrayList<>();
        for (Education e : educationList) {
            Map<String, Object> eduMap = new HashMap<>();
            eduMap.put("id", e.getId());
            eduMap.put("institution", e.getInstitution() != null ? e.getInstitution() : "N/A");
            eduMap.put("degree", e.getDegree() != null ? e.getDegree() : "N/A");
            eduMap.put("fieldOfStudy", e.getFieldOfStudy() != null ? e.getFieldOfStudy() : "N/A");
            eduMap.put("description", e.getDescription() != null ? e.getDescription() : "N/A");
            eduMap.put("startDate", e.getStartDate() != null ? e.getStartDate().toString() : "N/A");
            eduMap.put("endDate", e.getEndDate() != null ? e.getEndDate().toString() : "N/A");
            eduMap.put("gpa", e.getGpa() != null ? e.getGpa() : 0.0);
            eduDtoList.add(eduMap);
        }
        return ResponseEntity.ok(Collections.singletonMap("education", eduDtoList));
    }

    // 2. CREATE a new Education record
    @PostMapping
    public ResponseEntity<?> createEducation(@PathVariable("userId") int userId,
                                             @RequestBody EducationRequest req) {
        // Validate user
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Build the Education entity
        Education education = new Education();
        education.setUser(user);
        education.setInstitution(req.getInstitution());
        education.setDegree(req.getDegree());
        education.setFieldOfStudy(req.getFieldOfStudy());
        education.setDescription(req.getDescription());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            education.setStartDate(LocalDate.parse(req.getStartDate()));
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            education.setEndDate(LocalDate.parse(req.getEndDate()));
        }
        education.setGpa(req.getGpa() != null ? req.getGpa() : 0.0);

        // Save
        educationRepository.save(education);
        return ResponseEntity.ok("Created new education record");
    }

    // 3. UPDATE an Education record
    @PutMapping("/{eduId}")
    public ResponseEntity<?> updateEducation(@PathVariable("userId") int userId,
                                             @PathVariable("eduId") int eduId,
                                             @RequestBody EducationRequest req) {
        // Validate user
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Fetch the existing record
        Optional<Education> optionalEdu = educationRepository.findById(eduId);
        if (optionalEdu.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Education education = optionalEdu.get();
        // Ensure ownership
        if (education.getUser().getId() != userId) {
            return ResponseEntity.badRequest().body("This record doesn't belong to the specified user");
        }

        // Update fields
        education.setInstitution(req.getInstitution());
        education.setDegree(req.getDegree());
        education.setFieldOfStudy(req.getFieldOfStudy());
        education.setDescription(req.getDescription());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            education.setStartDate(LocalDate.parse(req.getStartDate()));
        } else {
            education.setStartDate(null);
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            education.setEndDate(LocalDate.parse(req.getEndDate()));
        } else {
            education.setEndDate(null);
        }
        education.setGpa(req.getGpa() != null ? req.getGpa() : 0.0);

        // Save
        educationRepository.save(education);
        return ResponseEntity.ok("Updated education record");
    }

    // 4. DELETE an Education record
    @DeleteMapping("/{eduId}")
    public ResponseEntity<?> deleteEducation(@PathVariable("userId") int userId,
                                             @PathVariable("eduId") int eduId) {
        // Validate user
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Fetch the record
        Optional<Education> optionalEdu = educationRepository.findById(eduId);
        if (optionalEdu.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Education education = optionalEdu.get();
        // Check ownership
        if (education.getUser().getId() != userId) {
            return ResponseEntity.badRequest().body("This record doesn't belong to the specified user");
        }

        // Delete
        educationRepository.deleteById(eduId);
        return ResponseEntity.ok("Deleted education record");
    }
}
