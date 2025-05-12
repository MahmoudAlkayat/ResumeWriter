package ninjas.cs490Project.dto;

import lombok.Data;

@Data
public class EducationData {
    private String institution = "N/A";
    private String degree = "N/A";
    private String fieldOfStudy = "N/A";
    private String startDate = "2000-01-01"; // Format: YYYY-MM-DD
    private String endDate = "N/A";
    private String description = "N/A";
    private double gpa = 0.0;
    private String location = "N/A";
}
