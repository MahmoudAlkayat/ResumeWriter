package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.service.CareerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes/career")
public class CareerController {

    @Autowired
    private CareerService careerService;

    // ------------------------------
    // Data Transfer Object (DTO)
    // ------------------------------
    public static class CareerRequest {
        private String title;
        private String company;
        private String startDate;
        private String endDate;
        private List<String> responsibilities;
        private List<String> accomplishments;
        private String location;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public List<String> getResponsibilities() { return responsibilities; }
        public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }

        public List<String> getAccomplishments() { return accomplishments; }
        public void setAccomplishments(List<String> accomplishments) { this.accomplishments = accomplishments; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    // 1. GET all WorkExperience records for a user
    @GetMapping
    public ResponseEntity<?> getCareerHistory(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("jobs", List.of()));
        }
        return ResponseEntity.ok(careerService.getCareerHistory(user));
    }

    // 2. CREATE a new WorkExperience record for a user
    @PostMapping
    public ResponseEntity<?> createCareer(@AuthenticationPrincipal User user, @RequestBody CareerRequest req) {
        careerService.createCareer(user, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/freeform")
    public ResponseEntity<?> createFreeformCareer(@AuthenticationPrincipal User user, @RequestBody Map<String, String> request) {
        careerService.createFreeformCareer(user, request.get("text"));
        return ResponseEntity.ok().build();
    }

    // 3. UPDATE an existing WorkExperience record for a user
    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateCareer(@AuthenticationPrincipal User user, @PathVariable int jobId, @RequestBody CareerRequest req) {
        careerService.updateCareer(user, jobId, req);
        return ResponseEntity.ok().build();
    }

    // 4. DELETE an existing WorkExperience record
    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteCareer(@AuthenticationPrincipal User user, @PathVariable int jobId) {
        careerService.deleteCareer(user, jobId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/freeform")
    public ResponseEntity<?> getFreeformCareer(@AuthenticationPrincipal User user) {
        List<Map<String, String>> freeformCareer = careerService.getFreeformCareer(user);
        return ResponseEntity.ok(freeformCareer);
    }

    @PutMapping("/freeform/{freeformId}")
    public ResponseEntity<?> updateFreeformCareer(@AuthenticationPrincipal User user, @PathVariable("freeformId") int freeformId, @RequestBody Map<String, String> request) {
        Map<String, Object> response = careerService.updateFreeformCareer(user, freeformId, request.get("text"));
        return ResponseEntity.ok(response);
    }
}
