package ninjas.cs490Project.service;

import ninjas.cs490Project.controller.JobDescriptionController.JobDescriptionRequest;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobDescriptionService {
    private final JobDescriptionRepository jobDescriptionRepository;

    public JobDescriptionService(JobDescriptionRepository jobDescriptionRepository) {
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    public Map<String, String> submitJobDescription(User user, JobDescriptionRequest jobDescription) {
        // Validate input
        if (jobDescription.getJobDescription().length() < 100) {
            throw new IllegalArgumentException("Job description must be at least 100 characters");
        }

        if (jobDescription.getJobTitle() != null && jobDescription.getJobTitle().length() > 255) {
            throw new IllegalArgumentException("Job title must not exceed 255 characters");
        }

        // Create and save job description
        JobDescription job = new JobDescription();
        job.setUser(user);
        job.setJobTitle(jobDescription.getJobTitle());
        job.setJobDescription(jobDescription.getJobDescription());
        job.setCreatedAt(Instant.now());
        JobDescription savedJob = jobDescriptionRepository.save(job);

        // Return response
        Map<String, String> response = new HashMap<>();
        response.put("jobId", String.valueOf(savedJob.getId()));
        response.put("status", "saved");
        return response;
    }

    public List<Map<String, Object>> getJobDescriptions(User user) {
        List<JobDescription> jobDescriptions = jobDescriptionRepository.findByUserOrderByCreatedAtDesc(user);
        List<Map<String, Object>> response = new ArrayList<>();

        for (JobDescription job : jobDescriptions) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("jobId", job.getId());
            jobMap.put("title", job.getJobTitle() != null ? job.getJobTitle().trim() : "");
            jobMap.put("text", job.getJobDescription() != null ? job.getJobDescription().trim() : "");
            jobMap.put("submittedAt", job.getCreatedAt());
            response.add(jobMap);
        }
        return response;
    }
}