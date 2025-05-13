package ninjas.cs490Project.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.objectMapper = new ObjectMapper()
                // allow single values (e.g. "Java, Python") to be read as arrays if needed
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
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

            JobDescription jobDescription = jobDescriptionRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

            List<WorkExperience> workExperiences = workExperienceRepository.findByUserId(user.getId());
            List<Education> educationList = educationRepository.findByUserId(user.getId());
            List<Skill> userSkills = skillService.getUserSkills(user);
            Profile profile = profileRepository.findByUser(user);

            String prompt = buildPrompt(jobDescription, workExperiences, educationList, user, userSkills, profile);

            GPTRequest gptRequest = new GPTRequest(
                    "gpt-3.5-turbo",
                    List.of(
                            new Message("system", "You are an expert resume writer and career consultant, specializing in optimizing resumes for Applicant Tracking Systems (ATS). Your task is to generate personalized, keyword-optimized resumes that align a candidate's experience and skills with a specific job description. Format your response strictly as JSON, following the provided schema. Do not include any extra commentary."),
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

            ResumeGenerationResult result = parseGptResponse(rawGptResponse, user, profile);

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
                    "Company: %s\nTitle: %s\nLocation: %s\nPeriod: %s to %s\nResponsibilities:\n%s\nAccomplishments:\n%s\n\n",
                    exp.getCompany(),
                    exp.getJobTitle(),
                    exp.getLocation() != null ? exp.getLocation() : "N/A",
                    exp.getStartDate(),
                    exp.getEndDate() != null ? exp.getEndDate() : "Present",
                    exp.getResponsibilities() != null ? String.join("\n", exp.getResponsibilities()) : "",
                    exp.getAccomplishments() != null ? String.join("\n", exp.getAccomplishments()) : ""
            ));
        }

        StringBuilder educationHistory = new StringBuilder();
        for (Education edu : educationList) {
            educationHistory.append(String.format(
                    "Institution: %s\nDegree: %s\nField: %s\nLocation: %s\nPeriod: %s to %s\nGPA: %s\nDescription: %s\n\n",
                    edu.getInstitution(),
                    edu.getDegree(),
                    edu.getFieldOfStudy(),
                    edu.getLocation() != null ? edu.getLocation() : "N/A",
                    edu.getStartDate(),
                    edu.getEndDate(),
                    edu.getGpa(),
                    edu.getDescription()
            ));
        }

        String jobTitle = jobDescription.getJobTitle() != null ? jobDescription.getJobTitle() : "";
        String jobDesc = jobDescription.getJobDescription() != null ? jobDescription.getJobDescription() : "";

        return String.format("""
            You are an expert resume writer with specialized knowledge in Applicant Tracking Systems (ATS). Your task is to generate a highly targeted resume based on the candidate's information, optimized for the following job description:

            JOB TITLE: %s
            JOB DESCRIPTION:
            %s

            CANDIDATE SKILLS:
            %s

            CANDIDATE'S WORK EXPERIENCE:
            %s

            CANDIDATE'S EDUCATION:
            %s

            Generate a resume in JSON format following this exact schema:
            {
              "skills": ["string","string",…],
              "educationList": [
                {
                  "institution": "string",
                  "degree": "string",
                  "fieldOfStudy": "string",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD",
                  "description": "string",
                  "gpa": number,
                  "location": "string"
                }
              ],
              "workExperienceList": [
                {
                  "company": "string",
                  "jobTitle": "string",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD",
                  "responsibilities": ["string", "string", ...],
                  "accomplishments": ["string", "string", ...],
                  "location": "string"
                }
              ]
            }

            Instructions:
            1. Extract 8–12 relevant keywords from the job description and integrate them meaningfully.
            2. For work experience:
               - DO NOT simply copy the original responsibilities and accomplishments
               - Rewrite and tailor each responsibility to highlight relevant skills and experiences that match the job requirements
               - Transform accomplishments to emphasize achievements that align with the job's needs
               - When including metrics and percentages:
                 * ALWAYS use real numbers from the original experience or reasonable estimates
                 * NEVER use placeholder values like 'X' or generic percentages
                 * If the original experience doesn't have specific numbers, either:
                   - Use the actual numbers from the original experience
                   - Make a reasonable estimate based on the context
                   - Focus on qualitative achievements if no numbers are available
                 * Examples of good metrics:
                   - "Reduced system downtime by 40 percent through implementation of automated monitoring"
                   - "Increased team productivity by five percentage points through process optimization"
                   - "Led a team of 12 developers to deliver a critical project two weeks ahead of schedule"
                   - "Optimized database queries resulting in 50 percent faster response times"
                 * Examples of what NOT to do:
                   - "Improved performance by X percent" (BAD - uses placeholder)
                   - "Reduced costs by X%%" (BAD - uses placeholder and %% symbol)
                   - "Increased efficiency by a significant amount" (BAD - too vague)
               - You may exclude work experience entries that are:
                 * Not relevant to the job requirements
                 * Too old (typically more than 10-15 years unless highly relevant)
                 * Redundant with more recent or more relevant experience
                 * Too numerous (aim for 3-5 most relevant experiences)
               - For each included experience:
                 * Keep responsibilities and accomplishments separate and distinct
                 * Responsibilities should describe daily tasks and duties
                 * Accomplishments should highlight specific achievements with metrics
                 * Include location for each work experience
                 * Each responsibility and accomplishment should be a separate string in the array
            3. For education:
               - Include location for each education entry
               - Format dates as YYYY-MM-DD
               - Include all education entries unless they are very old and irrelevant
            4. For skills:
               - Prioritize skills mentioned in the job description
               - Include both technical and soft skills that are relevant
               - Order skills by relevance to the job
               - Make sure "skills" is an actual JSON array
            5. General guidelines:
               - Maintain original job titles, company names, and date ranges
               - Ensure all dates maintain YYYY-MM-DD format
               - Use industry-specific terminology from the job description
               - Focus on transferable skills and experiences
               - Remove or de-emphasize irrelevant experiences
            6. Return ONLY the JSON with no additional commentary, markdown, or backticks.
            7. Use empty strings or 0 for any missing data, never null values.
            """,
                jobTitle,
                jobDesc,
                skillsSection.toString(),
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

    private ResumeGenerationResult parseGptResponse(String rawGptResponse, User user, Profile profile) throws Exception {
        try {
            var gptResponseObj = objectMapper.readValue(rawGptResponse, GPTResponse.class);
            String contentJson = gptResponseObj.getChoices().get(0).getMessage().getContent();
            ResumeGenerationResult result = objectMapper.readValue(contentJson, ResumeGenerationResult.class);

            // Sort work experiences by date (most recent first)
            if (result.getWorkExperienceList() != null) {
                result.getWorkExperienceList().sort((a, b) -> {
                    // Handle "Present" dates
                    String aEnd = a.getEndDate() != null && a.getEndDate().equals("Present") ? "9999-12-31" : a.getEndDate();
                    String bEnd = b.getEndDate() != null && b.getEndDate().equals("Present") ? "9999-12-31" : b.getEndDate();
                    
                    // Compare end dates first
                    int endDateCompare = bEnd.compareTo(aEnd);
                    if (endDateCompare != 0) {
                        return endDateCompare;
                    }
                    
                    // If end dates are equal, compare start dates
                    return b.getStartDate().compareTo(a.getStartDate());
                });
            }

            // Sort education entries by date (most recent first)
            if (result.getEducationList() != null) {
                result.getEducationList().sort((a, b) -> {
                    // Handle "Present" dates
                    String aEnd = a.getEndDate() != null && a.getEndDate().equals("Present") ? "9999-12-31" : a.getEndDate();
                    String bEnd = b.getEndDate() != null && b.getEndDate().equals("Present") ? "9999-12-31" : b.getEndDate();
                    
                    // Compare end dates first
                    int endDateCompare = bEnd.compareTo(aEnd);
                    if (endDateCompare != 0) {
                        return endDateCompare;
                    }
                    
                    // If end dates are equal, compare start dates
                    return b.getStartDate().compareTo(a.getStartDate());
                });
            }

            ResumeGenerationResult.PersonalInfo personalInfo = new ResumeGenerationResult.PersonalInfo();
            personalInfo.setFirstName(user.getFirstName());
            personalInfo.setLastName(user.getLastName());
            personalInfo.setEmail(user.getUsername());
            if (profile != null) {
                personalInfo.setPhone(profile.getPhone() != null ? profile.getPhone() : "");
                personalInfo.setAddress(profile.getAddress() != null ? profile.getAddress() : "");
            }
            result.setPersonalInfo(personalInfo);

            return result;
        } catch (Exception e) {
            logger.error("Error parsing GPT response: {}", e.getMessage());
            throw new Exception("Failed to generate resume. Please try again.");
        }
    }

    @Async
    public void generateResumeTest(User user, Long jobId, GeneratedResume savedResume, ProcessingStatus status) throws Exception {
        processingStatusService.startProcessing(status.getId());
        Thread.sleep(7000);

        ResumeGenerationResult mockResult = new ResumeGenerationResult();

        List<EducationData> mockEducationList = new ArrayList<>();
        EducationData mockEducation = new EducationData();
        mockEducation.setInstitution("University of Illinois at Urbana-Champaign");
        mockEducation.setDegree("Master of Science");
        mockEducation.setFieldOfStudy("Computer Science");
        mockEducation.setStartDate("2023-08-15");
        mockEducation.setEndDate("2025-05-15");
        mockEducation.setGpa(3.9);
        mockEducation.setLocation("Urbana, IL");
        mockEducation.setDescription("Specialized in Artificial Intelligence and Machine Learning");
        mockEducationList.add(mockEducation);
        mockResult.setEducationList(mockEducationList);

        List<WorkExperienceData> mockWorkExperienceList = new ArrayList<>();
        WorkExperienceData mockWorkExp1 = new WorkExperienceData();
        mockWorkExp1.setCompany("Tech Innovators Inc.");
        mockWorkExp1.setJobTitle("Senior Software Engineer");
        mockWorkExp1.setStartDate("2020-06-01");
        mockWorkExp1.setEndDate("2023-08-01");
        mockWorkExp1.setLocation("San Francisco, CA");
        mockWorkExp1.setResponsibilities(Arrays.asList(
            "Led development of cloud-native microservices using Spring Boot and Kubernetes",
            "Implemented CI/CD pipelines reducing deployment time by 60%",
            "Mentored junior developers and conducted code reviews"
        ));
        mockWorkExp1.setAccomplishments(Arrays.asList(
            "Reduced system downtime by 40% through implementation of automated monitoring",
            "Led a team of 5 developers to deliver a critical project 2 weeks ahead of schedule",
            "Optimized database queries resulting in 50% faster response times"
        ));
        mockWorkExperienceList.add(mockWorkExp1);

        WorkExperienceData mockWorkExp2 = new WorkExperienceData();
        mockWorkExp2.setCompany("Data Systems Corp");
        mockWorkExp2.setJobTitle("Software Developer");
        mockWorkExp2.setStartDate("2018-03-15");
        mockWorkExp2.setEndDate("2020-05-30");
        mockWorkExp2.setLocation("Seattle, WA");
        mockWorkExp2.setResponsibilities(Arrays.asList(
            "Developed and maintained RESTful APIs using Java and Spring Framework",
            "Optimized database queries improving application performance by 40%",
            "Collaborated with cross-functional teams to deliver features on schedule"
        ));
        mockWorkExp2.setAccomplishments(Arrays.asList(
            "Implemented automated testing reducing bug reports by 35%",
            "Designed and deployed a new caching system improving response times by 60%",
            "Received 'Employee of the Year' award for outstanding contributions"
        ));
        mockWorkExperienceList.add(mockWorkExp2);

        mockResult.setWorkExperienceList(mockWorkExperienceList);

        mockResult.setSkills(Arrays.asList(
                "Java", "Spring Boot", "Kubernetes", "Docker", "AWS",
                "Microservices", "REST APIs", "CI/CD", "Git", "SQL",
                "MongoDB", "React", "TypeScript", "Python", "Agile"
        ));

        Thread.sleep(2000);

        savedResume.setContent(objectMapper.writeValueAsString(mockResult));
        savedResume.setUpdatedAt(Instant.now());
        resumeService.storeGeneratedResume(savedResume);
        processingStatusService.completeProcessing(status.getId());
    }
}