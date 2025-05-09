package ninjas.cs490Project.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "formatted_resumes")
public class FormattedResume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "format_type")
    private String formatType;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_resume_id", nullable = false)
    private GeneratedResume generatedResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public GeneratedResume getGeneratedResume() {
        return generatedResume;
    }

    public void setGeneratedResume(GeneratedResume generatedResume) {
        this.generatedResume = generatedResume;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
} 