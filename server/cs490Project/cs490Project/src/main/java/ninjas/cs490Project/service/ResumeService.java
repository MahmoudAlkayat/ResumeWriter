package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeService {
    private final UploadedResumeRepository uploadedResumeRepository;
    private final GeneratedResumeRepository generatedResumeRepository;

    public ResumeService(UploadedResumeRepository uploadedResumeRepository,
                        GeneratedResumeRepository generatedResumeRepository) {
        this.uploadedResumeRepository = uploadedResumeRepository;
        this.generatedResumeRepository = generatedResumeRepository;
    }

    public UploadedResume storeUploadedResume(UploadedResume resume) {
        return uploadedResumeRepository.save(resume);
    }

    public GeneratedResume storeGeneratedResume(GeneratedResume resume) {
        return generatedResumeRepository.save(resume);
    }

    public List<UploadedResume> getUploadedResumesByUser(User user) {
        return uploadedResumeRepository.findByUser(user);
    }

    public List<GeneratedResume> getGeneratedResumesByUser(User user) {
        return generatedResumeRepository.findByUser(user);
    }
}

