package ninjas.cs490Project.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeParsingResult {
    private List<EducationData> educationList;
    private List<String> skills;
    private List<WorkExperienceData> workExperienceList; // NEW FIELD
}
