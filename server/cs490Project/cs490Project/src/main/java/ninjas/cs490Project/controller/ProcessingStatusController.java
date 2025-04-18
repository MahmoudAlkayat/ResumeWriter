package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.service.ProcessingStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes/status")
public class ProcessingStatusController {

    @Autowired
    private ProcessingStatusService processingStatusService;

    @GetMapping
    public ResponseEntity<List<ProcessingStatus>> getLatestStatuses(
            @RequestParam(defaultValue = "5") int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 5; // Default to 10 if invalid limit is provided
        }
        List<ProcessingStatus> statuses = processingStatusService.getLatestStatuses(limit);
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessingStatus> getStatusById(@PathVariable Long id) {
        ProcessingStatus status = processingStatusService.getStatusById(id);
        return ResponseEntity.ok(status);
    }
} 