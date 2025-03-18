package ninjas.cs490Project.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the parsed data from a resume, such as education details, skill names, and optionally work history and projects.
 */
public class ResumeData {

    private List<EducationData> educationList = new ArrayList<>();
    private List<String> skills = new ArrayList<>();

    // Optional additional fields:
    private List<String> workHistory = new ArrayList<>();
    private List<String> projects = new ArrayList<>();

    public List<EducationData> getEducationList() {
        return educationList;
    }

    public void setEducationList(List<EducationData> educationList) {
        this.educationList = educationList;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getWorkHistory() {
        return workHistory;
    }

    public void setWorkHistory(List<String> workHistory) {
        this.workHistory = workHistory;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }
}
