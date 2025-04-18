package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.repository.ProcessingStatusRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ninjas.cs490Project.entity.User;

import java.time.Instant;
import java.util.List;

@Service
public class ProcessingStatusService {
    
    @Autowired
    private ProcessingStatusRepository processingStatusRepository;

    public List<ProcessingStatus> getLatestStatuses(int limit) {
        return processingStatusRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "startedAt"))
        ).getContent();
    }

    public ProcessingStatus getStatusById(Long id) {
        return processingStatusRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Processing status not found"));
    }

    public ProcessingStatus createProcessingStatus(User user, ProcessingStatus.ProcessingType type, Long entityId) {
        ProcessingStatus status = new ProcessingStatus();
        status.setUser(user);
        status.setProcessingType(type);
        status.setEntityId(entityId);
        status.setStatus(ProcessingStatus.Status.PENDING);
        status.setStartedAt(Instant.now());
        return processingStatusRepository.save(status);
    }

    public ProcessingStatus updateStatus(Long statusId, ProcessingStatus.Status newStatus, String errorMessage) {
        ProcessingStatus status = getStatusById(statusId);
        status.setStatus(newStatus);
        if (errorMessage != null) {
            status.setErrorMessage(errorMessage);
        }
        if (newStatus == ProcessingStatus.Status.COMPLETED || newStatus == ProcessingStatus.Status.FAILED) {
            status.setCompletedAt(Instant.now());
        }
        return processingStatusRepository.save(status);
    }

    public ProcessingStatus startProcessing(Long statusId) {
        return updateStatus(statusId, ProcessingStatus.Status.PROCESSING, null);
    }

    public ProcessingStatus completeProcessing(Long statusId) {
        return updateStatus(statusId, ProcessingStatus.Status.COMPLETED, null);
    }

    public ProcessingStatus failProcessing(Long statusId, String errorMessage) {
        return updateStatus(statusId, ProcessingStatus.Status.FAILED, errorMessage);
    }
}