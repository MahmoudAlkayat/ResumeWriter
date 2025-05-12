package ninjas.cs490Project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkExperienceData {
    private String company;
    private String jobTitle;
    private String startDate;
    private String endDate;
    private List<String> responsibilities;
    private List<String> accomplishments;
    private String location;
}
