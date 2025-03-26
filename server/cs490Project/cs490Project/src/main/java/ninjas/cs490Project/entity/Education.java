package ninjas.cs490Project.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String institution;

    private String degree;

    @Column(name = "field_of_study")
    private String fieldOfStudy;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    // Getters and setters omitted for brevity
    public void setInstitution(String institution){
        this.institution = institution;
    }

    public void setDegree(String degree){
        this.degree = degree;
    }

    public void setFieldOfStudy(String fieldOfStudy){
        this.fieldOfStudy = fieldOfStudy;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public int getId(){
        return id;
    }
    public Resume getResume(){
        return resume;
    }
    public void setResume(Resume resume){
        this.resume = resume;
    }
}