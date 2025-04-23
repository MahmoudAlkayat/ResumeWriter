package ninjas.cs490Project.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeGenerationResult {
    private PersonalInfo personalInfo;
    private List<EducationData> educationList;
    private List<String> skills;
    private List<WorkExperienceData> workExperienceList;

    @Data
    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
    }
}
