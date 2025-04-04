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


    @Column(name = "GPA")
    private Double GPA;


    // Getters


    public int getId() {
        return id;
    }


    public String getInstitution() {
        return institution;
    }


    public String getDegree() {
        return degree;
    }


    public String getFieldOfStudy() {
        return fieldOfStudy;
    }


    public LocalDate getStartDate() {
        return startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }


    public String getDescription() {
        return description;
    }


    public Resume getResume() {
        return resume;
    }


    public Double getGpa(){
        return GPA;
    }


    // Setters


    public void setInstitution(String institution) {
        this.institution = institution;
    }


    public void setDegree(String degree) {
        this.degree = degree;
    }


    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }


    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }


    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setResume(Resume resume) {
        this.resume = resume;
    }


    public void setGpa(Double GPA){
        this.GPA = GPA;
    }
}

