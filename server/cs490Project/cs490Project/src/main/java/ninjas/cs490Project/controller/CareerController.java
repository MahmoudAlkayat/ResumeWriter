package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/resumes")
public class CareerController {

    private final WorkExperienceRepository workExperienceRepository;
    private final UserRepository userRepository;

    public CareerController(WorkExperienceRepository workExperienceRepository,
                            UserRepository userRepository) {
        this.workExperienceRepository = workExperienceRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/resumes/history?userId={userId}
     * Retrieves all career records for the given user.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getCareerHistory(@RequestParam("userId") int userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.ok(Collections.singletonMap("jobs", new ArrayList<>()));
        }
        // Assuming WorkExperienceRepository has a method findByResumeUserId
        List<WorkExperience> jobList = workExperienceRepository.findByResumeUserId(userId);

        List<Map<String, Object>> jobsDtoList = new ArrayList<>();
        for (WorkExperience job : jobList) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("id", job.getId());
            // Map jobTitle to title, description to responsibilities
            jobMap.put("title", job.getJobTitle() != null ? job.getJobTitle() : "N/A");
            jobMap.put("company", job.getCompany() != null ? job.getCompany() : "N/A");
            jobMap.put("startDate", job.getStartDate() != null ? job.getStartDate().toString() : "N/A");
            jobMap.put("endDate", job.getEndDate() != null ? job.getEndDate().toString() : "Present");
            jobMap.put("responsibilities", job.getDescription() != null ? job.getDescription() : "N/A");
            jobsDtoList.add(jobMap);
        }
        return ResponseEntity.ok(Collections.singletonMap("jobs", jobsDtoList));
    }

    // DTO for create/update requests
    public static class CareerRequest {
        private String title;
        private String company;
        private String startDate;
        private String endDate;
        private String responsibilities;

        // Getters and setters
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getCompany() {
            return company;
        }
        public void setCompany(String company) {
            this.company = company;
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
        public String getResponsibilities() {
            return responsibilities;
        }
        public void setResponsibilities(String responsibilities) {
            this.responsibilities = responsibilities;
        }
    }

    /**
     * POST /api/resumes/career?userId={userId}
     * Creates a new career record for the given user.
     */
    @PostMapping("/career")
    public ResponseEntity<?> createCareer(@RequestParam("userId") int userId,
                                          @RequestBody CareerRequest req) {
        // 1. Find the user
        User user = userRepository.findUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        // 2. Get the user's resume.
        // For simplicity, assume the user has at least one resume.
        Resume resume = user.getResumes().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No resume found for user"));

        // 3. Create the new WorkExperience record
        WorkExperience job = new WorkExperience();
        job.setJobTitle(req.getTitle());
        job.setCompany(req.getCompany());
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            job.setStartDate(LocalDate.parse(req.getStartDate()));
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            job.setEndDate(LocalDate.parse(req.getEndDate()));
        }
        job.setDescription(req.getResponsibilities());
        job.setResume(resume);

        workExperienceRepository.save(job);
        return ResponseEntity.ok("Created new career record");
    }

    /**
     * PUT /api/resumes/career/{jobId}
     * Updates an existing career record.
     */
    @PutMapping("/career/{jobId}")
    public ResponseEntity<?> updateCareer(@PathVariable("jobId") int jobId,
                                          @RequestBody CareerRequest req) {
        Optional<WorkExperience> optionalJob = workExperienceRepository.findById(jobId);
        if (!optionalJob.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        WorkExperience job = optionalJob.get();
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

        workExperienceRepository.save(job);
        return ResponseEntity.ok("Updated career record");
    }

    /**
     * DELETE /api/resumes/career/{jobId}
     * Deletes a career record.
     */
    @DeleteMapping("/career/{jobId}")
    public ResponseEntity<?> deleteCareer(@PathVariable("jobId") int jobId) {
        Optional<WorkExperience> optionalJob = workExperienceRepository.findById(jobId);
        if (!optionalJob.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        workExperienceRepository.deleteById(jobId);
        return ResponseEntity.ok("Deleted career record");
    }
}
