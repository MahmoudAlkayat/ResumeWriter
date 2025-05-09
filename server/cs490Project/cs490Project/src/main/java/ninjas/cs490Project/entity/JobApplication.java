package ninjas.cs490Project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
@Table(name = "job_applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"resume_id", "job_id"})
})
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private GeneratedResume resume;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobDescription job;

    @Column(nullable = false)
    private Instant appliedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = Instant.now();
    }
} 