package ninjas.cs490Project.service;


import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;


@Service
public class ResumeService {


    private final ResumeRepository resumeRepository;
    private final AsyncResumeParser asyncResumeParser;


    public ResumeService(ResumeRepository resumeRepository, AsyncResumeParser asyncResumeParser) {
        this.resumeRepository = resumeRepository;
        this.asyncResumeParser = asyncResumeParser;
    }


    // Saves a resume created from the uploaded file, title, and user.
    public Resume saveResume(MultipartFile file, String title, User user) throws IOException {
        // Read file bytes from the uploaded file
        byte[] fileBytes = file.getBytes();


        // Create and populate the Resume entity
        Resume resume = new Resume();
        resume.setTitle(title);
        resume.setFileData(fileBytes);
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
        resume.setUser(user);


        return resumeRepository.save(resume);
    }


    // This method stores the resume entity that has been built (with parsed details, etc.)
    public Resume storeResume(Resume resume) {
        return resumeRepository.save(resume);
    }

}

