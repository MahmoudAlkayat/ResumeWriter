package ninjas.cs490Project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.GPTRequest;
import ninjas.cs490Project.dto.Message;
import ninjas.cs490Project.dto.ResumeGenerationResult;
import ninjas.cs490Project.dto.EducationData;
import ninjas.cs490Project.dto.WorkExperienceData;
import ninjas.cs490Project.entity.Education;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.WorkExperience;
import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.Profile;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.JobDescriptionRepository;
import ninjas.cs490Project.repository.WorkExperienceRepository;
import ninjas.cs490Project.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class ResumeGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeGenerationService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ProcessingStatusService processingStatusService;
    private final ResumeService resumeService;
    private final SkillService skillService;
    private final ProfileRepository profileRepository;

    @Value("${gpt.api.key}")
    private String gptApiKey;

    public ResumeGenerationService(WebClient.Builder webClientBuilder,
                                 WorkExperienceRepository workExperienceRepository,
                                 EducationRepository educationRepository,
                                 JobDescriptionRepository jobDescriptionRepository,
                                 ProcessingStatusService processingStatusService,
                                 ResumeService resumeService,
                                 SkillService skillService,
                                 ProfileRepository profileRepository) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .build();
        this.objectMapper = new ObjectMapper();
        this.workExperienceRepository = workExperienceRepository;
        this.educationRepository = educationRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.processingStatusService = processingStatusService;
        this.resumeService = resumeService;
        this.skillService = skillService;
        this.profileRepository = profileRepository;
    }

    @Async
    public void generateResume(User user, Long jobId, GeneratedResume savedResume, ProcessingStatus status) throws Exception {
        try {
            processingStatusService.startProcessing(status.getId());
            // Get the job description
            JobDescription jobDescription = jobDescriptionRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

            // Get user's work experience and education
            List<WorkExperience> workExperiences = workExperienceRepository.findByUserId(user.getId());
            List<Education> educationList = educationRepository.findByUserId(user.getId());

            // Fetch user's skills
            List<Skill> userSkills = skillService.getUserSkills(user);
            // Fetch user's profile
            Profile profile = profileRepository.findByUser(user);

            // Build the prompt for GPT
            String prompt = buildPrompt(jobDescription, workExperiences, educationList, user, userSkills, profile);

            // Create the GPT request
            GPTRequest gptRequest = new GPTRequest(
                    "gpt-4",
                    List.of(
                            new Message("system", "You are an expert resume writer and career consultant, specializing in optimizing resumes for Applicant Tracking Systems (ATS). Your task is to generate personalized, keyword-optimized resumes that align a candidate’s experience and skills with a specific job description. Format your response strictly as JSON, following the provided schema. Do not include any extra commentary."),
                            new Message("user", prompt)
                    )
            );

            // Make the API call
            String rawGptResponse = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                    .bodyValue(gptRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse and return the result
            ResumeGenerationResult result = parseGptResponse(rawGptResponse);
            savedResume.setContent(objectMapper.writeValueAsString(result));
            savedResume.setUpdatedAt(Instant.now());
            resumeService.storeGeneratedResume(savedResume);
            processingStatusService.completeProcessing(status.getId());
        } catch (Exception e) {
            processingStatusService.failProcessing(status.getId(), e.getMessage());
            throw e;
        }
    }

    private String buildPrompt(JobDescription jobDescription, 
                         List<WorkExperience> workExperiences,
                         List<Education> educationList,
                         User user,
                         List<Skill> userSkills,
                         Profile profile) {
        StringBuilder contactInfo = new StringBuilder();
        contactInfo.append(String.format("First Name: %s\n", user.getFirstName()));
        contactInfo.append(String.format("Last Name: %s\n", user.getLastName()));
        contactInfo.append(String.format("Email: %s\n", user.getEmail()));
        if (profile != null) {
            contactInfo.append(String.format("Phone: %s\n", profile.getPhone() != null ? profile.getPhone() : ""));
            contactInfo.append(String.format("Address: %s\n", profile.getAddress() != null ? profile.getAddress() : ""));
        }

        StringBuilder skillsSection = new StringBuilder();
        if (userSkills != null && !userSkills.isEmpty()) {
            skillsSection.append("Skills: ");
            for (int i = 0; i < userSkills.size(); i++) {
                skillsSection.append(userSkills.get(i).getName());
                if (i < userSkills.size() - 1) {
                    skillsSection.append(", ");
                }
            }
            skillsSection.append("\n");
        }

        StringBuilder careerHistory = new StringBuilder();
        for (WorkExperience exp : workExperiences) {
            careerHistory.append(String.format(
                "Company: %s\nTitle: %s\nPeriod: %s to %s\nDescription: %s\n\n",
                exp.getCompany(),
                exp.getJobTitle(),
                exp.getStartDate(),
                exp.getEndDate() != null ? exp.getEndDate() : "Present",
                exp.getResponsibilities()
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
            You are an expert resume writer with specialized knowledge in Applicant Tracking Systems (ATS). Your task is to generate a highly targeted resume based on the candidate's information, optimized for the following job description:

            CANDIDATE CONTACT INFORMATION:
            %s

            CANDIDATE SKILLS:
            %s

            JOB TITLE: %s
            JOB DESCRIPTION:
            %s

            CANDIDATE'S WORK EXPERIENCE:
            %s

            CANDIDATE'S EDUCATION:
            %s

            Generate a resume in JSON format following this exact schema:
            {
            "contactInfo": {
                "firstName": string,
                "lastName": string,
                "email": string,
                "phone": string,
                "address": string
            },
            "skills": [string],
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

            Instructions:
            1. Extract 8–12 relevant keywords from the job description and integrate them meaningfully.
            2. Rewrite work experience bullet points with:
                - Strong action verbs
                - Quantifiable achievements
                - Concise 1–2 line format
            3. Prioritize skills most relevant to the job.
            4. Maintain original job titles, company names, and date ranges.
            5. Ensure all dates maintain YYYY-MM-DD format.
            6. Return ONLY the JSON with no additional commentary.
            7. Use empty strings or 0 for any missing data, never null values.

            Before returning the result, double-check:
            - JSON is valid and follows the schema
            - Tone is professional and confident
            - No extra text is included outside the JSON
            """,
            contactInfo.toString(),
            skillsSection.toString(),
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

    private ResumeGenerationResult parseGptResponse(String rawGptResponse) throws Exception {
        try {
            var gptResponseObj = objectMapper.readValue(rawGptResponse, GPTResponse.class);
            String contentJson = gptResponseObj.getChoices().get(0).getMessage().getContent();
            return objectMapper.readValue(contentJson, ResumeGenerationResult.class);
        } catch (Exception e) {
            logger.error("Error parsing GPT response: {}", e.getMessage());
            throw new Exception("Failed to generate resume. Please try again.");
        }
    }

    //Test with mock data
    @Async
    public void generateResumeTest(User user, Long jobId, GeneratedResume savedResume, ProcessingStatus status) throws Exception {
        processingStatusService.startProcessing(status.getId());
        Thread.sleep(7000);

        // TESTING: Use mock data instead of calling GPT API
        ResumeGenerationResult mockResult = new ResumeGenerationResult();

        // Mock education data
        List<EducationData> mockEducationList = new ArrayList<>();
        EducationData mockEducation = new EducationData();
        mockEducation.setInstitution("University of Illinois at Urbana-Champaign");
        mockEducation.setDegree("Master of Science");
        mockEducation.setFieldOfStudy("Computer Science");
        mockEducation.setStartDate("2023-08-15");
        mockEducation.setEndDate("2025-05-15");
        mockEducation.setGpa(3.9);
        mockEducation.setDescription("Specialized in Artificial Intelligence and Machine Learning");
        mockEducationList.add(mockEducation);
        mockResult.setEducationList(mockEducationList);

        // Mock work experience data
        List<WorkExperienceData> mockWorkExperienceList = new ArrayList<>();
        
        WorkExperienceData mockWorkExp1 = new WorkExperienceData();
        mockWorkExp1.setCompany("Tech Innovators Inc.");
        mockWorkExp1.setJobTitle("Senior Software Engineer");
        mockWorkExp1.setStartDate("2020-06-01");
        mockWorkExp1.setEndDate("2023-08-01");
        mockWorkExp1.setResponsibilities("• Led development of cloud-native microservices using Spring Boot and Kubernetes\n" +
                                  "• Implemented CI/CD pipelines reducing deployment time by 60%\n" +
                                  "• Mentored junior developers and conducted code reviews");
        mockWorkExperienceList.add(mockWorkExp1);

        WorkExperienceData mockWorkExp2 = new WorkExperienceData();
        mockWorkExp2.setCompany("Data Systems Corp");
        mockWorkExp2.setJobTitle("Software Developer");
        mockWorkExp2.setStartDate("2018-03-15");
        mockWorkExp2.setEndDate("2020-05-30");
        mockWorkExp2.setResponsibilities("• Developed and maintained RESTful APIs using Java and Spring Framework\n" +
                                  "• Optimized database queries improving application performance by 40%\n" +
                                  "• Collaborated with cross-functional teams to deliver features on schedule");
        mockWorkExperienceList.add(mockWorkExp2);

        mockResult.setWorkExperienceList(mockWorkExperienceList);

        // Mock skills
        mockResult.setSkills(Arrays.asList(
            "Java", "Spring Boot", "Kubernetes", "Docker", "AWS",
            "Microservices", "REST APIs", "CI/CD", "Git", "SQL",
            "MongoDB", "React", "TypeScript", "Python", "Agile"
        ));

        
        // Simulate API delay
        Thread.sleep(2000);
        
        savedResume.setContent(objectMapper.writeValueAsString(mockResult));
        savedResume.setUpdatedAt(Instant.now());
        resumeService.storeGeneratedResume(savedResume);
        processingStatusService.completeProcessing(status.getId());
    }
} 