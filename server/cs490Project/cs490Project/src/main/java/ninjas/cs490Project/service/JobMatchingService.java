package ninjas.cs490Project.service;

import ninjas.cs490Project.dto.ResumeData;
import ninjas.cs490Project.entity.JobListing;
import ninjas.cs490Project.repository.JobListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private static final Logger logger = LoggerFactory.getLogger(JobMatchingService.class);

    @Autowired
    private JobListingRepository jobListingRepository;

    public List<JobListing> findMatchingJobs(ResumeData resumeData) {
        try {
            logger.info("Starting job matching for resume data: {}", resumeData);

            // Retrieve all job listings
            List<JobListing> allJobs = jobListingRepository.findAll();
            logger.info("Retrieved {} job listings from repository", allJobs.size());

            // Parse resume skills to a standardized format
            List<String> resumeSkills = resumeData.getSkills().stream()
                    .map(String::trim)
                    .map(skill -> skill.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());
            logger.info("Parsed resume skills: {}", resumeSkills);

            // Filter jobs by checking if at least one resume skill matches any required skill
            List<JobListing> matchingJobs = allJobs.stream()
                    .filter(job -> {
                        String requiredSkills = job.getRequiredSkills();
                        if (requiredSkills == null || requiredSkills.isEmpty()) {
                            return false;
                        }
                        // Split job's required skills into a list
                        List<String> jobSkills = List.of(requiredSkills.split(",")).stream()
                                .map(String::trim)
                                .map(s -> s.toLowerCase(Locale.ROOT))
                                .collect(Collectors.toList());
                        boolean matches = resumeSkills.stream().anyMatch(jobSkills::contains);
                        if (matches) {
                            // Assuming JobListing has a getId() method; adjust if necessary
                            logger.debug("Job with id {} matches with resume skills", job.getId());
                        }
                        return matches;
                    })
                    .collect(Collectors.toList());

            logger.info("Found {} matching job listings", matchingJobs.size());
            return matchingJobs;
        } catch (Exception e) {
            logger.error("Error occurred while matching jobs", e);
            throw e;
        }
    }
}
