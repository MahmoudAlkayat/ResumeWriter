package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UploadedResumeRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class ResumeService {
    private final UploadedResumeRepository uploadedResumeRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final AsyncResumeParser asyncResumeParser;

    public ResumeService(UploadedResumeRepository uploadedResumeRepository,
                        GeneratedResumeRepository generatedResumeRepository,
                        AsyncResumeParser asyncResumeParser) {
        this.uploadedResumeRepository = uploadedResumeRepository;
        this.generatedResumeRepository = generatedResumeRepository;
        this.asyncResumeParser = asyncResumeParser;
    }

    // Saves a resume created from the uploaded file, title, and user.
    public UploadedResume saveUploadedResume(MultipartFile file, String title, User user) throws IOException {
        // Read file bytes from the uploaded file
        byte[] fileBytes = file.getBytes();

        // Create and populate the Resume entity
        UploadedResume resume = new UploadedResume();
        resume.setTitle(title);
        resume.setFileData(fileBytes);
        resume.setCreatedAt(Instant.now());
        resume.setUpdatedAt(Instant.now());
        resume.setUser(user);

        return uploadedResumeRepository.save(resume);
    }

    // This method stores the resume entity that has been built (with parsed details, etc.)
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

