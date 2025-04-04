package ninjas.cs490Project.dto;

import lombok.Data;

@Data
public class WorkExperienceData {
    private String company;
    private String jobTitle;
    private String startDate;
    private String endDate;
    private String description;
}
