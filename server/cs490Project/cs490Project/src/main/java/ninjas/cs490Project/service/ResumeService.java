package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    public Resume storeResume(Resume resume) {
        // Additional business logic or validations can be added here.
        return resumeRepository.save(resume);
    }
}
