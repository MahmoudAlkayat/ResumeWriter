package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/users/{userId}/career")
public class CareerController {

    private final WorkExperienceRepository workExperienceRepository;
    private final UserRepository userRepository;

    public CareerController(WorkExperienceRepository workExperienceRepository,
                            UserRepository userRepository) {
        this.workExperienceRepository = workExperienceRepository;
        this.userRepository = userRepository;
    }

    // ------------------------------
    // Data Transfer Object (DTO)
    // ------------------------------
    public static class CareerRequest {
        private String title;
        private String company;
        private String startDate;
        private String endDate;
        private String responsibilities;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public String getResponsibilities() { return responsibilities; }
        public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }
    }

    // 1. GET all WorkExperience records for a user
    @GetMapping
    public ResponseEntity<?> getCareerHistory(@PathVariable("userId") int userId) {
        // Ensure user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            // Return empty if user not found
            return ResponseEntity.ok(Collections.singletonMap("jobs", new ArrayList<>()));
        }

        // Retrieve all WorkExperience for this user
        List<WorkExperience> jobList = workExperienceRepository.findByUserId(userId);

        // Convert to a DTO-like structure
        List<Map<String, Object>> jobsDtoList = new ArrayList<>();
        for (WorkExperience job : jobList) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("id", job.getId());
            jobMap.put("title", job.getJobTitle() != null ? job.getJobTitle() : "N/A");
            jobMap.put("company", job.getCompany() != null ? job.getCompany() : "N/A");
            jobMap.put("startDate", job.getStartDate() != null ? job.getStartDate().toString() : "N/A");
            jobMap.put("endDate", job.getEndDate() != null ? job.getEndDate().toString() : "Present");
            jobMap.put("responsibilities", job.getDescription() != null ? job.getDescription() : "N/A");
            jobsDtoList.add(jobMap);
        }
        return ResponseEntity.ok(Collections.singletonMap("jobs", jobsDtoList));
    }

    // 2. CREATE a new WorkExperience record for a user
    @PostMapping
    public ResponseEntity<?> createCareer(@PathVariable("userId") int userId,
                                          @RequestBody CareerRequest req) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Create and populate a new WorkExperience entity
        WorkExperience job = new WorkExperience();
        job.setUser(user);
        job.setJobTitle(req.getTitle());
        job.setCompany(req.getCompany());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            job.setStartDate(LocalDate.parse(req.getStartDate()));
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            job.setEndDate(LocalDate.parse(req.getEndDate()));
        }
        job.setDescription(req.getResponsibilities());

        // Save the entity
        workExperienceRepository.save(job);
        return ResponseEntity.ok("Created new career record");
    }

    // 3. UPDATE an existing WorkExperience record for a user
    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateCareer(@PathVariable("userId") int userId,
                                          @PathVariable("jobId") int jobId,
                                          @RequestBody CareerRequest req) {
        // Ensure user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Check if the WorkExperience record exists
        Optional<WorkExperience> optionalJob = workExperienceRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        WorkExperience job = optionalJob.get();
        // Ensure it belongs to the user in the path
        if (job.getUser().getId() != userId) {
            return ResponseEntity.badRequest().body("This record doesn't belong to the specified user");
        }

        // Update the record
        job.setJobTitle(req.getTitle());
        job.setCompany(req.getCompany());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            job.setStartDate(LocalDate.parse(req.getStartDate()));
        } else {
            job.setStartDate(null);
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            job.setEndDate(LocalDate.parse(req.getEndDate()));
        } else {
            job.setEndDate(null);
        }
        job.setDescription(req.getResponsibilities());

        // Save the changes
        workExperienceRepository.save(job);
        return ResponseEntity.ok("Updated career record");
    }

    // 4. DELETE an existing WorkExperience record
    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteCareer(@PathVariable("userId") int userId,
                                          @PathVariable("jobId") int jobId) {
        // Ensure user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Retrieve the record
        Optional<WorkExperience> optionalJob = workExperienceRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        WorkExperience job = optionalJob.get();
        // Check ownership
        if (job.getUser().getId() != userId) {
            return ResponseEntity.badRequest().body("This record doesn't belong to the specified user");
        }

        // Delete
        workExperienceRepository.delete(job);
        return ResponseEntity.ok("Deleted career record");
    }
}
