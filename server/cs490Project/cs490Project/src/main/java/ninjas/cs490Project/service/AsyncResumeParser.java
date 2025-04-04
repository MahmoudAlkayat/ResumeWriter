package ninjas.cs490Project.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.entity.Resume;
import ninjas.cs490Project.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.ByteArrayInputStream;
import java.util.Optional;


@Service
public class AsyncResumeParser {


    private final ResumeRepository resumeRepository;
    private final ResumeParsingService resumeParsingService;
    private final ObjectMapper objectMapper;


    public AsyncResumeParser(ResumeRepository resumeRepository, ResumeParsingService resumeParsingService) {
        this.resumeRepository = resumeRepository;
        this.resumeParsingService = resumeParsingService;
        this.objectMapper = new ObjectMapper();
    }


    @Async
    public void parseResume(Long resumeId, byte[] fileData) {
        try {
            // Retrieve the resume from the repository
            Optional<Resume> optionalResume = resumeRepository.findById(resumeId);
            if (optionalResume.isEmpty()) {
                System.err.println("Resume with ID " + resumeId + " not found.");
                return;
            }
            Resume resume = optionalResume.get();


            // Use Apache Tika to extract text from the file data
            Tika tika = new Tika();
            String resumeText = tika.parseToString(new ByteArrayInputStream(fileData));


            // Use your ResumeParsingService to extract key information via GPT
            ResumeParsingResult parsingResult = resumeParsingService.parseKeyInformation(resumeText);


            // Optionally update the resume record with the extracted text (or parsed data)
            resume.setContent(resumeText);
            // Here you could also update related fields such as education and skills if needed.


            // Save the updated resume record
            resumeRepository.save(resume);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

