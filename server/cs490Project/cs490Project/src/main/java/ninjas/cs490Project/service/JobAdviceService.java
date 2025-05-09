package ninjas.cs490Project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.GPTRequest;
import ninjas.cs490Project.dto.Message;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.repository.GeneratedResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Service
public class JobAdviceService {
    private static final Logger logger = LoggerFactory.getLogger(JobAdviceService.class);

    private final JobDescriptionRepository jobDescriptionRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gpt.api.key}")
    private String gptApiKey;

    public JobAdviceService(
            JobDescriptionRepository jobDescriptionRepository,
            GeneratedResumeRepository generatedResumeRepository,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.generatedResumeRepository = generatedResumeRepository;
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.objectMapper = objectMapper;
    }

    public String generateAdvice(Long jobId, Long resumeId) {
        try {
            // Retrieve job description and resume
            JobDescription jobDescription = jobDescriptionRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
            GeneratedResume resume = generatedResumeRepository.findById(resumeId)
                    .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

            String prompt = buildPrompt(jobDescription, resume);

            String advice = callGptApi(prompt);

            return advice;
        } catch (Exception e) {
            logger.error("Error generating job advice: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate job advice: " + e.getMessage());
        }
    }

    private String buildPrompt(JobDescription jobDescription, GeneratedResume resume) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional career advisor. Analyze the following job description and resume, then provide specific, actionable advice to help the candidate improve their chances of getting this job.\n\n");
        
        prompt.append("JOB DESCRIPTION:\n");
        prompt.append(jobDescription.getJobDescription()).append("\n\n");
        
        prompt.append("RESUME:\n");
        prompt.append(resume.getContent()).append("\n\n");
        
        prompt.append("Please provide advice that includes:\n");
        prompt.append("1. How well the resume matches the job requirements\n");
        prompt.append("2. Specific suggestions for improving the resume\n");
        prompt.append("3. Tips for tailoring the resume to this position\n");
        prompt.append("4. Interview preparation suggestions based on the job requirements\n\n");
        
        prompt.append("IMPORTANT: Return ONLY the advice text in plain language. Do not include any JSON schemas, markdown formatting, or additional markup. Do not include section headers or bullet points. Write the advice as a cohesive, well-structured paragraph that flows naturally. Focus on actionable feedback that the candidate can implement immediately.");

        return prompt.toString();
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

    private String callGptApi(String prompt) {
        try {
            GPTRequest gptRequest = new GPTRequest(
                "gpt-4",
                List.of(
                    new Message("system", "You are a professional career advisor specializing in resume optimization and job application strategy. Provide clear, actionable advice based on the job description and resume analysis. Return only plain text advice without any formatting, headers, or markup."),
                    new Message("user", prompt)
                )
            );

            String rawGptResponse = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            var gptResponseObj = objectMapper.readValue(rawGptResponse, GPTResponse.class);
            return gptResponseObj.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            logger.error("Error calling GPT API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate advice from GPT API");
        }
    }
}