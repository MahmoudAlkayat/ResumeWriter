package ninjas.cs490Project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.dto.WorkExperienceData;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.repository.ResumeRepository;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsyncResumeParser {

    private static final Logger logger = LoggerFactory.getLogger(AsyncResumeParser.class);

    private final ResumeRepository resumeRepository;
    private final ResumeParsingService resumeParsingService;
    private final WorkExperienceRepository workExperienceRepository;
    private final ObjectMapper objectMapper;

    public AsyncResumeParser(ResumeRepository resumeRepository,
                             ResumeParsingService resumeParsingService,
                             WorkExperienceRepository workExperienceRepository) {
        this.resumeRepository = resumeRepository;
        this.resumeParsingService = resumeParsingService;
        this.workExperienceRepository = workExperienceRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Async
    @Transactional
    public void parseResume(Resume resume) {
        try {
            // Extract text using Apache Tika
            Tika tika = new Tika();
            String resumeText = tika.parseToString(new ByteArrayInputStream(resume.getFileData()));
            logger.info("Extracted resume text (first 100 chars): {}",
                    resumeText.substring(0, Math.min(resumeText.length(), 100)));

            // Parse resume details using your parsing service (e.g., GPT)
            ResumeParsingResult parsingResult = resumeParsingService.parseKeyInformation(resumeText);
            logger.info("Parsed result: {}", objectMapper.writeValueAsString(parsingResult));

            // Update the resume content (and possibly education or skills)
            resume.setContent(resumeText);
            resumeRepository.save(resume);

            // Process work experience entries (mapped by User)
            List<WorkExperienceData> workExpList = parsingResult.getWorkExperienceList();
            if (workExpList != null && !workExpList.isEmpty()) {
                logger.info("Found {} work experience entries.", workExpList.size());
                List<WorkExperience> workExperiences = new ArrayList<>();

                for (WorkExperienceData data : workExpList) {
                    WorkExperience we = new WorkExperience();
                    we.setCompany(data.getCompany());
                    we.setJobTitle(data.getJobTitle());
                    we.setStartDate(LocalDate.parse(data.getStartDate()));

                    String endDateStr = data.getEndDate();
                    if (endDateStr == null || endDateStr.trim().isEmpty() || endDateStr.equalsIgnoreCase("N/A")) {
                        we.setEndDate(null);
                    } else {
                        we.setEndDate(LocalDate.parse(endDateStr));
                    }

                    we.setDescription(data.getDescription());
                    // Map directly by User (not by Resume)
                    we.setUser(resume.getUser());
                    workExperiences.add(we);
                }
                // Save all work experience entries in a batch
                workExperienceRepository.saveAll(workExperiences);
                logger.info("Saved {} work experience entries.", workExperiences.size());
            } else {
                logger.warn("No work experience entries found in parsed result.");
            }
        } catch (Exception e) {
            logger.error("Error processing resume with ID " + resume.getId(), e);
        }
    }
}
