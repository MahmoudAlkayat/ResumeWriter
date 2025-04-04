package ninjas.cs490Project.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Table(name = "resumes")
public class Resume {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private String title;


    // Field to store extracted text content (if needed)
    @Column(columnDefinition = "TEXT")
    private String content;


    // Field to store the resume file bytes as a BLOB; using LONGBLOB to allow larger files
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;


    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // Relationship: Many resumes belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // Relationship: Resume to skills (many-to-many)
    @ManyToMany
    @JoinTable(
            name = "resume_skills",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;


    // Relationship: One resume can have many work experiences
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<WorkExperience> workExperiences;

    // Relationship: One resume can have many education records
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Education> educationRecords;


    // Getters and Setters


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public byte[] getFileData() {
        return fileData;
    }


    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public Set<Skill> getSkills() {
        return skills;
    }


    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }


    public Set<WorkExperience> getWorkExperiences() {
        return workExperiences;
    }


    public void setWorkExperiences(Set<WorkExperience> workExperiences) {
        this.workExperiences = workExperiences;
    }


    public Set<Education> getEducationRecords() {
        return educationRecords;
    }


    public void setEducationRecords(Set<Education> educationRecords) {
        this.educationRecords = educationRecords;
    }
}

