package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.CareerService;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes/career")
public class CareerController {

    private final UserRepository userRepository;
    private final CareerService careerService;

    public CareerController(UserRepository userRepository,
                          CareerService careerService) {
        this.userRepository = userRepository;
        this.careerService = careerService;
    }

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
    public ResponseEntity<?> getCareerHistory(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.ok(Map.of("jobs", List.of()));
        }
        return ResponseEntity.ok(careerService.getCareerHistory(user));
    }

    // 2. CREATE a new WorkExperience record for a user
    @PostMapping
    public ResponseEntity<?> createCareer(Authentication authentication,
                                          @RequestBody CareerRequest req) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            careerService.createCareer(user, req);
            return ResponseEntity.ok("Created new career record");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/freeform")
    public ResponseEntity<?> createFreeformCareer(Authentication authentication,
                                                 @RequestBody Map<String, String> request) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            Map<String, Object> response = careerService.createFreeformCareer(user, request.get("text"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. UPDATE an existing WorkExperience record for a user
    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateCareer(Authentication authentication,
                                          @PathVariable("jobId") int jobId,
                                          @RequestBody CareerRequest req) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            careerService.updateCareer(user, jobId, req);
            return ResponseEntity.ok("Updated career record");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. DELETE an existing WorkExperience record
    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteCareer(Authentication authentication,
                                          @PathVariable("jobId") int jobId) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            careerService.deleteCareer(user, jobId);
            return ResponseEntity.ok("Deleted career record");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/freeform")
    public ResponseEntity<?> getFreeformCareer(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            List<Map<String, String>> response = careerService.getFreeformCareer(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/freeform/{freeformId}")
    public ResponseEntity<?> updateFreeformCareer(Authentication authentication,
                                                @PathVariable("freeformId") int freeformId,
                                                @RequestBody Map<String, String> request) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            Map<String, Object> response = careerService.updateFreeformCareer(user, freeformId, request.get("text"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
