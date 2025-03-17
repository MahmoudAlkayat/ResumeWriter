package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.ResumeData;
import ninjas.cs490Project.entity.JobListing;
import ninjas.cs490Project.repository.JobListingRepository;
import ninjas.cs490Project.service.TailoredResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RestController
@RequestMapping("/api/resumes")
public class TailoredResumeController {

    private static final Logger logger = LoggerFactory.getLogger(TailoredResumeController.class);

    @Autowired
    private JobListingRepository jobListingRepository;

    @Autowired
    private TailoredResumeService tailoredResumeService;

    /**
     * Generates a tailored resume PDF for the specified job listing.
     * Expects the resume data in the request body and a job listing ID as a path variable.
     * Returns the generated PDF as an attachment.
     */
    @PostMapping("/tailored/{jobId}")
    public ResponseEntity<?> generateTailoredResume(
            @PathVariable("jobId") Long jobId,
            @RequestBody ResumeData resumeData) {
        Optional<JobListing> jobOptional = jobListingRepository.findById(jobId);
        if (!jobOptional.isPresent()) {
            logger.warn("Job listing not found for jobId: {}", jobId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job listing not found.");
        }
        JobListing jobListing = jobOptional.get();

        try {
            logger.info("Generating tailored resume for job id: {}", jobId);
            // Here we pass the resumeData's skills as the matchingSkills list.
            byte[] pdfBytes = tailoredResumeService.generateTailoredPdf(resumeData, jobListing, resumeData.getSkills());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tailored_resume.pdf");

            logger.info("Tailored resume generated successfully for job id: {}", jobId);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating tailored resume for job id: {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating tailored resume: " + e.getMessage());
        }
    }
}
