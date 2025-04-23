package ninjas.cs490Project.service;

import ninjas.cs490Project.controller.EducationController.EducationRequest;
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.EducationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EducationService {
    private final EducationRepository educationRepository;

    public EducationService(EducationRepository educationRepository) {
        this.educationRepository = educationRepository;
    }

    public Map<String, Object> getAllEducation(User user) {
        List<Education> educationList = educationRepository.findByUserId(user.getId());
        List<Map<String, Object>> eduDtoList = new ArrayList<>();

        for (Education e : educationList) {
            Map<String, Object> eduMap = new HashMap<>();
            eduMap.put("id", e.getId());
            eduMap.put("institution", e.getInstitution() != null ? e.getInstitution() : "N/A");
            eduMap.put("degree", e.getDegree() != null ? e.getDegree() : "N/A");
            eduMap.put("fieldOfStudy", e.getFieldOfStudy() != null ? e.getFieldOfStudy() : "N/A");
            eduMap.put("description", e.getDescription() != null ? e.getDescription() : "N/A");
            eduMap.put("startDate", e.getStartDate() != null ? e.getStartDate().toString() : "");
            eduMap.put("endDate", e.getEndDate() != null ? e.getEndDate().toString() : "");
            eduMap.put("gpa", e.getGpa() != null ? e.getGpa() : 0.0);
            eduDtoList.add(eduMap);
        }
        return Collections.singletonMap("education", eduDtoList);
    }

    public void createEducation(User user, EducationRequest req) {
        // Validate required fields
        if (req.getInstitution() == null || req.getInstitution().trim().isEmpty()) {
            throw new IllegalArgumentException("Institution is required");
        }
        if (req.getDegree() == null || req.getDegree().trim().isEmpty()) {
            throw new IllegalArgumentException("Degree is required");
        }

        Education education = new Education();
        education.setUser(user);
        education.setInstitution(req.getInstitution());
        education.setDegree(req.getDegree());
        education.setFieldOfStudy(req.getFieldOfStudy());
        education.setDescription(req.getDescription());

        try {
            if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
                education.setStartDate(LocalDate.parse(req.getStartDate()));
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid start date format. Please use YYYY-MM-DD format");
        }

        try {
            if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
                education.setEndDate(LocalDate.parse(req.getEndDate()));
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid end date format. Please use YYYY-MM-DD format");
        }

        education.setGpa(req.getGpa() != null ? req.getGpa() : 0.0);
        educationRepository.save(education);
    }

    @Transactional
    public void updateEducation(User user, int eduId, EducationRequest req) {
        // Validate required fields
        if (req.getInstitution() == null || req.getInstitution().trim().isEmpty()) {
            throw new IllegalArgumentException("Institution is required");
        }
        if (req.getDegree() == null || req.getDegree().trim().isEmpty()) {
            throw new IllegalArgumentException("Degree is required");
        }

        Optional<Education> optionalEdu = educationRepository.findById(eduId);
        if (optionalEdu.isEmpty()) {
            throw new IllegalArgumentException("Education record not found");
        }

        Education education = optionalEdu.get();
        if (education.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("This record doesn't belong to the specified user");
        }

        education.setInstitution(req.getInstitution());
        education.setDegree(req.getDegree());
        education.setFieldOfStudy(req.getFieldOfStudy());
        education.setDescription(req.getDescription());

        try {
            if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
                education.setStartDate(LocalDate.parse(req.getStartDate()));
            } else {
                education.setStartDate(null);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid start date format. Please use YYYY-MM-DD format");
        }

        try {
            if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
                education.setEndDate(LocalDate.parse(req.getEndDate()));
            } else {
                education.setEndDate(null);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid end date format. Please use YYYY-MM-DD format");
        }

        education.setGpa(req.getGpa() != null ? req.getGpa() : 0.0);
        educationRepository.save(education);
    }

    @Transactional
    public void deleteEducation(User user, int eduId) {
        Optional<Education> optionalEdu = educationRepository.findById(eduId);
        if (optionalEdu.isEmpty()) {
            throw new IllegalArgumentException("Education record not found");
        }

        Education education = optionalEdu.get();
        if (education.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("This record doesn't belong to the specified user");
        }

        educationRepository.delete(education);
    }
} 
