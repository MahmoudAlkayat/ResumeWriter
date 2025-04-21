package ninjas.cs490Project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkExperienceData {
    private String company;
    private String jobTitle;
    private String startDate;
    private String endDate;
    private String responsibilities;
    private String accomplishments;
}
