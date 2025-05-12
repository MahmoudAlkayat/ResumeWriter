package ninjas.cs490Project.service;

import ninjas.cs490Project.controller.CareerController.CareerRequest;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.entity.FreeformEntry;
import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import ninjas.cs490Project.repository.FreeformEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CareerService {
    private final WorkExperienceRepository workExperienceRepository;
    private final FreeformEntryRepository freeformEntryRepository;
    private final ProcessingStatusService processingStatusService;
    private final AsyncResumeParser asyncResumeParser;

    public CareerService(WorkExperienceRepository workExperienceRepository,
                        FreeformEntryRepository freeformEntryRepository,
                        ProcessingStatusService processingStatusService,
                        AsyncResumeParser asyncResumeParser) {
        this.workExperienceRepository = workExperienceRepository;
        this.freeformEntryRepository = freeformEntryRepository;
        this.processingStatusService = processingStatusService;
        this.asyncResumeParser = asyncResumeParser;
    }

    public Map<String, Object> getCareerHistory(User user) {
        List<WorkExperience> jobList = workExperienceRepository.findByUserId(user.getId());
        List<Map<String, Object>> jobsDtoList = new ArrayList<>();
        
        for (WorkExperience job : jobList) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("id", job.getId());
            jobMap.put("title", job.getJobTitle() != null ? job.getJobTitle() : "N/A");
            jobMap.put("company", job.getCompany() != null ? job.getCompany() : "N/A");
            jobMap.put("startDate", job.getStartDate() != null ? job.getStartDate().toString() : "");
            jobMap.put("endDate", job.getEndDate() != null ? job.getEndDate().toString() : "");
            jobMap.put("responsibilities", job.getResponsibilities() != null ? job.getResponsibilities() : new ArrayList<>());
            jobMap.put("accomplishments", job.getAccomplishments() != null ? job.getAccomplishments() : new ArrayList<>());
            jobMap.put("location", job.getLocation() != null ? job.getLocation() : "N/A");
            jobsDtoList.add(jobMap);
        }
        
        return Collections.singletonMap("jobs", jobsDtoList);
    }

    public void createCareer(User user, CareerRequest req) {
        // Validate required fields
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required");
        }
        if (req.getCompany() == null || req.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("Company is required");
        }
        if (req.getStartDate() == null || req.getStartDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Start date is required");
        }

        WorkExperience job = new WorkExperience();
        job.setUser(user);
        job.setJobTitle(req.getTitle());
        job.setCompany(req.getCompany());
        job.setLocation(req.getLocation());

        try {
            job.setStartDate(LocalDate.parse(req.getStartDate()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid start date format. Please use YYYY-MM-DD format");
        }

        try {
            job.setEndDate(req.getEndDate() != null && !req.getEndDate().isEmpty() ? 
                LocalDate.parse(req.getEndDate()) : null);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid end date format. Please use YYYY-MM-DD format");
        }

        job.setResponsibilities(req.getResponsibilities() != null ? req.getResponsibilities() : new ArrayList<>());
        job.setAccomplishments(req.getAccomplishments() != null ? req.getAccomplishments() : new ArrayList<>());

        workExperienceRepository.save(job);
    }

    public Map<String, Object> createFreeformCareer(User user, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text field is required");
        }

        FreeformEntry entry = new FreeformEntry();
        entry.setUser(user);
        entry.setRawText(text);
        entry.setCreatedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        FreeformEntry savedEntry = freeformEntryRepository.save(entry);

        ProcessingStatus status = processingStatusService.createProcessingStatus(
            user,
            ProcessingStatus.ProcessingType.FREEFORM_ENTRY,
            Long.valueOf(savedEntry.getId())
        );
        
        processingStatusService.startProcessing(status.getId());
        asyncResumeParser.parseFreeformCareer(text, user, savedEntry, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Freeform career entry submitted for processing");
        response.put("statusId", status.getId());
        response.put("entryId", savedEntry.getId());
        return response;
    }

    public void updateCareer(User user, int jobId, CareerRequest req) {
        // Validate required fields
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required");
        }
        if (req.getCompany() == null || req.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("Company is required");
        }
        if (req.getStartDate() == null || req.getStartDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Start date is required");
        }

        Optional<WorkExperience> optionalJob = workExperienceRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            throw new IllegalArgumentException("Career record not found");
        }

        WorkExperience job = optionalJob.get();
        if (job.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("This record doesn't belong to the specified user");
        }

        job.setJobTitle(req.getTitle());
        job.setCompany(req.getCompany());
        job.setLocation(req.getLocation());

        try {
            job.setStartDate(LocalDate.parse(req.getStartDate()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid start date format. Please use YYYY-MM-DD format");
        }

        try {
            job.setEndDate(req.getEndDate() != null && !req.getEndDate().isEmpty() ? 
                LocalDate.parse(req.getEndDate()) : null);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid end date format. Please use YYYY-MM-DD format");
        }

        job.setResponsibilities(req.getResponsibilities() != null ? req.getResponsibilities() : new ArrayList<>());
        job.setAccomplishments(req.getAccomplishments() != null ? req.getAccomplishments() : new ArrayList<>());

        workExperienceRepository.save(job);
    }

    @Transactional
    public void deleteCareer(User user, int jobId) {
        Optional<WorkExperience> optionalJob = workExperienceRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            throw new IllegalArgumentException("Career record not found");
        }

        WorkExperience job = optionalJob.get();
        if (job.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("This record doesn't belong to the specified user");
        }

        FreeformEntry freeformEntry = job.getFreeformEntry();
        if (freeformEntry != null) {
            freeformEntryRepository.delete(freeformEntry);
        }

        workExperienceRepository.delete(job);
    }

    public List<Map<String, String>> getFreeformCareer(User user) {
        List<FreeformEntry> entries = freeformEntryRepository.findByUserId(user.getId());
        List<Map<String, String>> response = new ArrayList<>();
        
        for (FreeformEntry entry : entries) {
            Map<String, String> entryMap = new HashMap<>();
            entryMap.put("id", String.valueOf(entry.getId()));
            entryMap.put("text", entry.getRawText());
            entryMap.put("updatedAt", String.valueOf(entry.getUpdatedAt()));
            entryMap.put("careerId", entry.getWorkExperience() != null ? 
                String.valueOf(entry.getWorkExperience().getId()) : null);
            response.add(entryMap);
        }
        
        return response;
    }

    @Transactional
    public Map<String, Object> updateFreeformCareer(User user, int freeformId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text field is required");
        }

        Optional<FreeformEntry> optionalEntry = freeformEntryRepository.findById(freeformId);
        if (optionalEntry.isEmpty()) {
            throw new IllegalArgumentException("Freeform entry not found");
        }

        FreeformEntry entry = optionalEntry.get();
        if (entry.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("This entry doesn't belong to the specified user");
        }

        if (text.equals(entry.getRawText())) {
            throw new IllegalArgumentException("No changes were made to the freeform entry");
        }

        entry.setRawText(text);
        entry.setUpdatedAt(Instant.now());
        freeformEntryRepository.save(entry);

        ProcessingStatus status = processingStatusService.createProcessingStatus(
            user,
            ProcessingStatus.ProcessingType.FREEFORM_ENTRY,
            Long.valueOf(freeformId)
        );

        processingStatusService.startProcessing(status.getId());
        asyncResumeParser.parseFreeformCareer(text, user, entry, status);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Freeform career entry updated and submitted for processing");
        response.put("statusId", status.getId());
        response.put("entryId", freeformId);
        return response;
    }
} 