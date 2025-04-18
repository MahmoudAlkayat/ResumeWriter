package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.service.ProcessingStatusService;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/resumes/status")
public class ProcessingStatusController {

    @Autowired
    private ProcessingStatusService processingStatusService;

    @Autowired
    private UploadedResumeRepository uploadedResumeRepository;

    @Autowired
    private GeneratedResumeRepository generatedResumeRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLatestStatuses(
            @RequestParam(defaultValue = "5") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 5; // Default to 10 if invalid limit is provided
        }
        List<ProcessingStatus> statuses = processingStatusService.getLatestStatuses(limit);
        List<Map<String, Object>> response = new ArrayList<>();
        for (ProcessingStatus status : statuses) {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("id", status.getId());
            statusMap.put("type", status.getProcessingType());
            if (status.getProcessingType() == ProcessingStatus.ProcessingType.UPLOADED_RESUME) {
                String resumeName = uploadedResumeRepository.findById(status.getEntityId()).get().getTitle();
                statusMap.put("resumeName", resumeName);
            }
            if (status.getProcessingType() == ProcessingStatus.ProcessingType.GENERATED_RESUME) {
                String jobTitleString = generatedResumeRepository.findById(status.getEntityId()).get().getJobDescription().getJobTitle();
                if (jobTitleString != null) {
                    statusMap.put("jobTitle", jobTitleString);
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
    public ResponseEntity<Map<String, Object>> getStatusById(@PathVariable Long id) {
        ProcessingStatus status = processingStatusService.getStatusById(id);
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