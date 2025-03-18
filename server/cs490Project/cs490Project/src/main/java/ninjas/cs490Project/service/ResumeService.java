package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    @Autowired
    private ResumeRepository resumeRepository;

    public Resume storeResume(Resume resume) {
        if (resume == null) {
            throw new IllegalArgumentException("Resume cannot be null");
        }
        logger.info("Storing resume for user with ID: {}", resume.getUser().getId());
        Resume savedResume = resumeRepository.save(resume);
        logger.info("Resume stored successfully with ID: {}", savedResume.getId());
        return savedResume;
    }
}
