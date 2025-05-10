package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.JobApplication;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.repository.JobApplicationRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class JobApplicationService {
    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private GeneratedResumeRepository resumeRepository;

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Transactional
    public Map<String, Object> recordApplication(User user, Long resumeId, Long jobId) {
        // Validate resume exists and belongs to user
        GeneratedResume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        if (resume.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("Resume does not belong to user");
        }

        // Validate job exists and belongs to user
        JobDescription job = jobDescriptionRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
        if (job.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("Job description does not belong to user");
        }

        // Check for duplicate application
        if (jobApplicationRepository.existsByResumeIdAndJobId(resumeId, jobId)) {
            throw new IllegalArgumentException("Application already exists for this resume and job");
        }

        // Create and save application
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setResume(resume);
        application.setJob(job);
        application = jobApplicationRepository.save(application);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("applicationId", String.valueOf(application.getId()));
        response.put("status", "saved");
        response.put("appliedAt", application.getAppliedAt().toString());
        return response;
    }

    public List<Map<String, Object>> getApplicationHistory(User user) {
        return jobApplicationRepository.findByUserOrderByAppliedAtDesc(user)
            .stream()
            .map(application -> {
                Map<String, Object> record = new HashMap<>();
                record.put("applicationId", String.valueOf(application.getId()));
                record.put("resumeId", String.valueOf(application.getResume().getId()));
                record.put("jobId", String.valueOf(application.getJob().getId()));
                record.put("appliedAt", application.getAppliedAt().toString());
                
                if (application.getJob().getJobTitle() != null) {
                    record.put("jobTitle", application.getJob().getJobTitle());
                }
                return record;
            })
            .collect(Collectors.toList());
    }
} 