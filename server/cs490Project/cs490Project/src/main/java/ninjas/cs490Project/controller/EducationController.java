package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
public class EducationController {

    private final EducationRepository educationRepository;
    private final UserRepository userRepository;

    public EducationController(EducationRepository educationRepository,
                               UserRepository userRepository) {
        this.educationRepository = educationRepository;
        this.userRepository = userRepository;
    }

    // 1. GET all education for a user
    @GetMapping("/education")
    public ResponseEntity<?> getEducation(@RequestParam("userId") int userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            // Return an empty array if user not found, or return 404 if you prefer
            return ResponseEntity.ok(Collections.singletonMap("education", new ArrayList<>()));
        }

        // Find all Education linked to this user's resumes
        // This requires a custom query in EducationRepository:
        //   List<Education> findByResumeUserId(int userId);
        List<Education> educationList = educationRepository.findByResumeUserId(userId);

        List<Map<String, Object>> eduDtoList = new ArrayList<>();
        for (Education e : educationList) {
            Map<String, Object> eduMap = new HashMap<>();
            eduMap.put("id", e.getId());  // Include the ID so you can edit/delete by ID
            eduMap.put("degree", e.getDegree() != null ? e.getDegree() : "N/A");
            eduMap.put("institution", e.getInstitution() != null ? e.getInstitution() : "N/A");
            eduMap.put("startDate", e.getStartDate() != null ? e.getStartDate().toString() : "N/A");
            eduMap.put("endDate", e.getEndDate() != null ? e.getEndDate().toString() : "N/A");
            eduMap.put("gpa", e.getGpa() != null ? e.getGpa() : 0.0);
            // If needed:
            // eduMap.put("fieldOfStudy", e.getFieldOfStudy() != null ? e.getFieldOfStudy() : "N/A");
            // eduMap.put("description", e.getDescription() != null ? e.getDescription() : "N/A");
            eduDtoList.add(eduMap);
        }
        return ResponseEntity.ok(Collections.singletonMap("education", eduDtoList));
    }

    // 2. CREATE (POST) a new education record
    // Example: POST /api/resumes/education?userId=1
    @PostMapping("/education")
    public ResponseEntity<?> createEducation(
            @RequestParam("userId") int userId,
            @RequestBody EducationRequest req
    ) {
        // 2a. Find the user
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // 2b. Usually you'd want to find or create a Resume for that user
        //     For this example, let's assume a single Resume per user or do a custom fetch.
        //     If you're not storing the resume yet, you can adapt as needed.
        Resume resume = user.getResumes().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No resume found for user"));

        // 2c. Build the new Education entity
        Education education = new Education();
        education.setDegree(req.getDegree());
        education.setInstitution(req.getInstitution());

        // parse dates from string, or handle "N/A"
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            education.setStartDate(LocalDate.parse(req.getStartDate()));
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            education.setEndDate(LocalDate.parse(req.getEndDate()));
        }
        education.setGpa(req.getGpa() != null ? req.getGpa() : 0.0);

        // Link the resume
        education.setResume(resume);

        // 2d. Save it
        educationRepository.save(education);
        return ResponseEntity.ok("Created new education record");
    }

    // 3. UPDATE (PUT) an existing education record
    // Example: PUT /api/resumes/education/123
    @PutMapping("/education/{eduId}")
    public ResponseEntity<?> updateEducation(
            @PathVariable("eduId") int eduId,
            @RequestBody EducationRequest req
    ) {
        Optional<Education> optionalEdu = educationRepository.findById(eduId);
        if (optionalEdu.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Education education = optionalEdu.get();

        // Update fields
        education.setDegree(req.getDegree());
        education.setInstitution(req.getInstitution());

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

        educationRepository.save(education);
        return ResponseEntity.ok("Updated education record");
    }

    // 4. DELETE an existing education record
    // Example: DELETE /api/resumes/education/123
    @DeleteMapping("/education/{eduId}")
    public ResponseEntity<?> deleteEducation(@PathVariable("eduId") int eduId) {
        Optional<Education> optionalEdu = educationRepository.findById(eduId);
        if (optionalEdu.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        educationRepository.deleteById(eduId);
        return ResponseEntity.ok("Deleted education record");
    }

    // This is a DTO class to handle create/update requests
    public static class EducationRequest {
        private String degree;
        private String institution;
        private String startDate;
        private String endDate;
        private Double gpa;
        // Add fieldOfStudy, description, etc. if needed

        // getters and setters
        public String getDegree() {
            return degree;
        }
        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getInstitution() {
            return institution;
        }
        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public String getStartDate() {
            return startDate;
        }
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public Double getGpa() {
            return gpa;
        }
        public void setGpa(Double gpa) {
            this.gpa = gpa;
        }
    }
}
