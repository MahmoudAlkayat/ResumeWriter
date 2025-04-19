package ninjas.cs490Project.service;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import ninjas.cs490Project.dto.GPTRequest;
import ninjas.cs490Project.dto.Message;
import ninjas.cs490Project.dto.ResumeParsingResult;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.List;


@Service
public class ResumeParsingService {


    private static final Logger logger = LoggerFactory.getLogger(ResumeParsingService.class);


    private final WebClient webClient;
    private final ObjectMapper objectMapper;


    @Value("${gpt.api.key}")
    private String gptApiKey;


    public ResumeParsingService(WebClient.Builder webClientBuilder) {
        // Configure the WebClient with logging filters
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .filter(logRequest())
                .filter(logResponse())
                .build();


        this.objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to support Java 8 date/time types (e.g., LocalDate)
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Ignore unknown properties globally
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }


    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response status: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientResponse);
        });
    }


    /**
     * Extracts plain text from the uploaded resume file using Apache Tika.
     */
    public String extractTextFromFile(MultipartFile file) throws Exception {
        Tika tika = new Tika();
        // Convert file's InputStream to String
        return tika.parseToString(file.getInputStream());
    }


    /**
     * Calls the GPT API to parse key information from the extracted resume text.
     */
    public ResumeParsingResult parseKeyInformation(String resumeText) throws Exception {


        // Build your prompt
        String prompt =
        """
        You are an assistant that parses resume text into a strict JSON structure. Your task is to extract education, skills, and work experience data exactly as specified below. Return only valid JSON, with no additional commentary, markdown, or backticks.
        
        1. Output a JSON object with exactly three keys: "educationList", "skills", and "workExperienceList".
        
        2. "educationList" is an array (never null) of objects. Each object must include:
           {
             "institution": string (default "N/A"),
             "degree": string (default "N/A"),
             "fieldOfStudy": string (default "N/A"),
             "startDate": string in YYYY-MM-DD format (default "2000-01-01"),
             "endDate": string in YYYY-MM-DD format (default "N/A"),
             "description": string (default "N/A"),
             "gpa": number (default 0)
           }
           If no education data is found, return an empty array [].
        
        3. "skills" is an array (never null) of strings. If no skills are found, return an empty array [].
        
        4. "workExperienceList" is an array (never null) of objects. Each object must include:
           {
             "company": string (default "N/A"),
             "jobTitle": string (default "N/A"),
             "startDate": string in YYYY-MM-DD format (default "2000-01-01"),
             "endDate": string in YYYY-MM-DD format (default "N/A"),
             "responsibilities": string (default "N/A"),
             "accomplishments": string (default "N/A")
           }
           If no work experience is found, return an empty array [].
        
        5. Always include all specified keys. Do not add extra properties. No null values.
        
        6. If certain fields cannot be determined, use the default values provided.
        
        7. For work experience:
           - Responsibilities should list daily tasks and duties
           - Accomplishments should highlight specific achievements, metrics, and impact
           - Format both as bullet points or short paragraphs
           - Keep responsibilities and accomplishments separate and distinct
        
        Now parse the following resume text and produce only a valid JSON response (no extra text or formatting):
        """ + resumeText;


        // Create the GPT request payload
        GPTRequest gptRequest = new GPTRequest(
                "gpt-4",
                List.of(
                        new Message("developer", "You are a helpful assistant."),
                        new Message("user", prompt)
                )
        );



        // 1) Make the API call to get the raw JSON response
        String rawGptResponse = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        // 2) Print out the raw GPT response to the console
        System.out.println("GPT RAW RESPONSE:\n" + rawGptResponse);


        // 3) Log the same raw GPT response using SLF4J logger
        logger.info("GPT RAW RESPONSE:\n{}", rawGptResponse);


        // 4) Parse the top-level GPTResponse (which has choices -> message -> content)
        GPTResponse gptResponseObj = objectMapper.readValue(rawGptResponse, GPTResponse.class);


        // 5) Extract the JSON string from "content"
        String contentJson = gptResponseObj
                .getChoices().get(0)       // we only use the first choice
                .getMessage()
                .getContent();            // raw JSON string for your resume data


        // 6) Now parse that string into ResumeParsingResult
        ResumeParsingResult parsedResult = objectMapper.readValue(contentJson, ResumeParsingResult.class);


        // 7) Return the fully-parsed ResumeParsingResult
        return parsedResult;
    }

    public ResumeParsingResult parseFreeformCareer(String text) throws Exception {
        // Build your prompt for freeform career parsing
        String prompt =
        """
        You are an assistant that parses freeform career text into a strict JSON structure. Your task is to extract work experience data exactly as specified below. Return only valid JSON, with no additional commentary, markdown, or backticks.
        
        Output a JSON object with exactly one key: "workExperienceList" which is an array of objects. Each object must include:
        {
          "company": string (default "N/A"),
          "jobTitle": string (default "N/A"),
          "startDate": string in YYYY-MM-DD format (default "2000-01-01"),
          "endDate": string in YYYY-MM-DD format (default "N/A"),
          "responsibilities": string (default "N/A"),
          "accomplishments": string (default "N/A")
        }
        
        Rules:
        1. Extract dates in YYYY-MM-DD format. If only year is available, use YYYY-01-01.
        2. If end date is not specified but context suggests current position, use "Present".
        3. Responsibilities should list daily tasks and duties
        4. Accomplishments should highlight specific achievements, metrics, and impact
        5. Format both responsibilities and accomplishments as bullet points or short paragraphs
        6. Keep responsibilities and accomplishments separate and distinct
        7. If certain fields cannot be determined, use the default values provided.
        8. Always include all specified keys. Do not add extra properties. No null values.
        9. You may reformat text to ensure clarity but must not omit relevant information.
        
        Now parse the following career text and produce only a valid JSON response (no extra text or formatting):
        """ + text;

        // Create the GPT request payload
        GPTRequest gptRequest = new GPTRequest(
                "gpt-4",
                List.of(
                        new Message("developer", "You are a helpful assistant."),
                        new Message("user", prompt)
                )
        );

        // Make the API call to get the raw JSON response
        String rawGptResponse = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptApiKey)
                .bodyValue(gptRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse the GPT response
        GPTResponse gptResponseObj;
        try {
            gptResponseObj = objectMapper.readValue(rawGptResponse, GPTResponse.class);
        } catch (Exception e) {
            logger.error("Error parsing GPT response: {}", e.getMessage());
            logger.error("Raw GPT response: {}", rawGptResponse);
            throw new Exception("Failed to parse GPT response. Please try again.");
        }

        String contentJson;
        try {
            contentJson = gptResponseObj
                    .getChoices().get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            logger.error("Error extracting content from GPT response: {}", e.getMessage());
            logger.error("GPT response object: {}", gptResponseObj);
            throw new Exception("Failed to extract content from GPT response. Please try again.");
        }

        // Parse into ResumeParsingResult
        try {
            ResumeParsingResult parsedResult = objectMapper.readValue(contentJson, ResumeParsingResult.class);
            return parsedResult;
        } catch (Exception e) {
            logger.error("Error parsing freeform career JSON: {}", e.getMessage());
            logger.error("Content JSON: {}", contentJson);
            throw new Exception("Failed to parse career information. Please ensure your input contains clear work experience details.");
        }
    }
}


// ---------------------------------------------------------------------------
// Classes representing the top-level GPT response structure


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class GPTResponse {
    private List<Choice> choices;
}


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Choice {
    private GPTMessage message;
}


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class GPTMessage {
    private String role;
    private String content; // The JSON we really want is here
}

