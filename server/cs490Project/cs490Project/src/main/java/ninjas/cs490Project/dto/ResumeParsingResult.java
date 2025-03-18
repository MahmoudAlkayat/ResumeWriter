package ninjas.cs490Project.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the parsed results from a resume,
 * such as a list of education records, skills, etc.
 */
public class ResumeParsingResult {

    // A list of education entries captured from the resume
    private List<EducationData> educationList = new ArrayList<>();

    // A list of skill names captured from the resume
    private List<String> skills = new ArrayList<>();

    // Potentially more fields for things like work experience, certifications, etc.
    // private List<WorkExperienceData> workExperienceList;
    // private List<CertificationData> certifications;
    // ... etc.

    /**
     * Returns the list of EducationData objects extracted from the resume.
     */
    public List<EducationData> getEducationList() {
        return educationList;
    }

    /**
     * Sets the list of EducationData objects extracted from the resume.
     * Typically assigned by your parsing service or NLP logic.
     */
    public void setEducationList(List<EducationData> educationList) {
        this.educationList = educationList;
    }

    /**
     * Returns a list of skills extracted from the resume.
     * This could be simple strings like ["Java", "C++", "Python"]
     * or you may want a separate DTO/Entity if you store them more richly.
     */
    public List<String> getSkills() {
        return skills;
    }

    /**
     * Sets the list of extracted skills.
     * Typically assigned by your parsing service or NLP logic.
     */
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    // If you add more fields for experience, certifications, etc.,
    // add their getters and setters below.
}
