package ninjas.cs490Project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.GPTRequest;
import ninjas.cs490Project.dto.Message;
import ninjas.cs490Project.dto.ResumeParsingResult;
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Service
public class ResumeGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeGenerationService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    @Value("${gpt.api.key}")
    private String gptApiKey;

    public ResumeGenerationService(WebClient.Builder webClientBuilder,
                                 WorkExperienceRepository workExperienceRepository,
                                 EducationRepository educationRepository,
                                 JobDescriptionRepository jobDescriptionRepository) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .build();
        this.objectMapper = new ObjectMapper();
        this.workExperienceRepository = workExperienceRepository;
        this.educationRepository = educationRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    public ResumeParsingResult generateResume(User user, Long jobId) throws Exception {
        // 1. Get the job description
        JobDescription jobDescription = jobDescriptionRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

        // 2. Get user's work experience and education
        List<WorkExperience> workExperiences = workExperienceRepository.findByUserId(user.getId());
        List<Education> educationList = educationRepository.findByUserId(user.getId());

        // 3. Build the prompt for GPT
        String prompt = buildPrompt(jobDescription, workExperiences, educationList);

        // 4. Create the GPT request
        GPTRequest gptRequest = new GPTRequest(
                "gpt-4",
                List.of(
                        new Message("developer", "You are a helpful assistant."),
                        new Message("user", prompt)
                )
        );

        // 5. Make the API call
        String rawGptResponse = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 6. Parse and return the result
        return parseGptResponse(rawGptResponse);
    }

    private String buildPrompt(JobDescription jobDescription, 
                             List<WorkExperience> workExperiences,
                             List<Education> educationList) {
        StringBuilder careerHistory = new StringBuilder();
        for (WorkExperience exp : workExperiences) {
            careerHistory.append(String.format(
                "Company: %s\nTitle: %s\nPeriod: %s to %s\nDescription: %s\n\n",
                exp.getCompany(),
                exp.getJobTitle(),
                exp.getStartDate(),
                exp.getEndDate() != null ? exp.getEndDate() : "Present",
                exp.getDescription()
            ));
        }

        StringBuilder educationHistory = new StringBuilder();
        for (Education edu : educationList) {
            educationHistory.append(String.format(
                "Institution: %s\nDegree: %s\nField: %s\nPeriod: %s to %s\nGPA: %s\nDescription: %s\n\n",
                edu.getInstitution(),
                edu.getDegree(),
                edu.getFieldOfStudy(),
                edu.getStartDate(),
                edu.getEndDate(),
                edu.getGpa(),
                edu.getDescription()
            ));
        }

        return String.format("""
            You are an expert resume writer. Your task is to generate a targeted resume based on the candidate's experience and education,
            optimized for the following job description:

            JOB TITLE: %s
            JOB DESCRIPTION:
            %s

            CANDIDATE'S WORK EXPERIENCE:
            %s

            CANDIDATE'S EDUCATION:
            %s

            Generate a resume in JSON format following this exact schema:
            {
              "educationList": [
                {
                  "institution": string,
                  "degree": string,
                  "fieldOfStudy": string,
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD",
                  "description": string,
                  "gpa": number
                }
              ],
              "skills": [string],
              "workExperienceList": [
                {
                  "company": string,
                  "jobTitle": string,
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD",
                  "description": string
                }
              ]
            }

            IMPORTANT RULES:
            1. Tailor the experience descriptions to highlight relevance to the job description
            2. Keep the same dates and company names, but optimize descriptions
            3. Extract and list relevant skills based on both experience and job requirements
            4. Return ONLY the JSON, no other text
            5. Ensure all dates are in YYYY-MM-DD format
            6. Never include null values, use empty strings or 0 for missing data
            """,
            jobDescription.getJobTitle(),
            jobDescription.getJobDescription(),
            careerHistory.toString(),
            educationHistory.toString()
        );
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GPTResponse {
        private List<Choice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {
        private GPTMessage message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GPTMessage {
        private String role;
        private String content;
    }

    private ResumeParsingResult parseGptResponse(String rawGptResponse) throws Exception {
        try {
            var gptResponseObj = objectMapper.readValue(rawGptResponse, GPTResponse.class);
            String contentJson = gptResponseObj.getChoices().get(0).getMessage().getContent();
            return objectMapper.readValue(contentJson, ResumeParsingResult.class);
        } catch (Exception e) {
            logger.error("Error parsing GPT response: {}", e.getMessage());
            throw new Exception("Failed to generate resume. Please try again.");
        }
    }
} 