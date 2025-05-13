package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.FreeformEntry;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.service.ProcessingStatusService;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.FreeformEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resumes/status")
public class ProcessingStatusController {

    @Autowired
    private ProcessingStatusService processingStatusService;

    @Autowired
    private UploadedResumeRepository uploadedResumeRepository;

    @Autowired
    private GeneratedResumeRepository generatedResumeRepository;

    @Autowired
    private FreeformEntryRepository freeformEntryRepository;


    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLatestStatuses(@AuthenticationPrincipal User user, @RequestParam(defaultValue = "5") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 5; // Default to 5 if invalid limit is provided
        }

        List<ProcessingStatus> statuses = processingStatusService.getLatestStatusesForUser(user, limit);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (ProcessingStatus status : statuses) {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("id", status.getId());
            statusMap.put("type", status.getProcessingType());
            if (status.getProcessingType() == ProcessingStatus.ProcessingType.UPLOADED_RESUME) {
                Optional<UploadedResume> uploadedResume = uploadedResumeRepository.findById(status.getEntityId());
                if (uploadedResume.isPresent()) {
                    String resumeName = uploadedResume.get().getTitle();
                    statusMap.put("resumeName", resumeName);
                }
            }
            if (status.getProcessingType() == ProcessingStatus.ProcessingType.GENERATED_RESUME) {
                Optional<GeneratedResume> generatedResume = generatedResumeRepository.findById(status.getEntityId());
                if (generatedResume.isPresent()) {
                    String jobTitleString = generatedResume.get().getJobDescription().getJobTitle();
                    if (jobTitleString != null) {
                        statusMap.put("jobTitle", jobTitleString);
                    } else { // No title, default to ID
                        statusMap.put("jobId", generatedResume.get().getJobDescription().getId());
                    }

                    if (generatedResume.get().getTitle() != null) {
                        statusMap.put("resumeName", generatedResume.get().getTitle());
                    }
                }
            }
            if (status.getProcessingType() == ProcessingStatus.ProcessingType.FREEFORM_ENTRY) {
                Optional<FreeformEntry> freeformEntry = freeformEntryRepository.findById(status.getEntityId().intValue());
                if (freeformEntry.isPresent() && freeformEntry.get().getWorkExperience() != null) {
                    Integer careerId = freeformEntry.get().getWorkExperience().getId();
                    statusMap.put("careerId", careerId);
                }
            }
            statusMap.put("status", status.getStatus());
            statusMap.put("startedAt", status.getStartedAt());
            statusMap.put("completedAt", status.getCompletedAt());
            statusMap.put("error", status.getErrorMessage());
            response.add(statusMap);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStatusById(@AuthenticationPrincipal User user, @PathVariable Long id) {
        ProcessingStatus status = processingStatusService.getStatusById(id);
        
        // Check if the status belongs to the current user
        if (status.getUser().getId() != user.getId()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("id", status.getId());
        statusMap.put("type", status.getProcessingType());
        statusMap.put("status", status.getStatus());
        statusMap.put("startedAt", status.getStartedAt());
        statusMap.put("completedAt", status.getCompletedAt());
        statusMap.put("error", status.getErrorMessage());
        return ResponseEntity.ok(statusMap);
    }
} 