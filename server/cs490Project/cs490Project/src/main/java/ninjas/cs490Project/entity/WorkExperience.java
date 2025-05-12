package ninjas.cs490Project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "work_experience")
public class WorkExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "freeform_entry_id")
    private FreeformEntry freeformEntry;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_experience_responsibilities", joinColumns = @JoinColumn(name = "work_experience_id"))
    @Column(name = "responsibility", columnDefinition = "TEXT")
    private List<String> responsibilities;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_experience_accomplishments", joinColumns = @JoinColumn(name = "work_experience_id"))
    @Column(name = "accomplishment", columnDefinition = "TEXT")
    private List<String> accomplishments;

    private String location;

    // Getters and Setters

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }
    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<String> getAccomplishments() {
        return accomplishments;
    }
    public void setAccomplishments(List<String> accomplishments) {
        this.accomplishments = accomplishments;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public FreeformEntry getFreeformEntry() {
        return freeformEntry;
    }
    public void setFreeformEntry(FreeformEntry freeformEntry) {
        this.freeformEntry = freeformEntry;
    }
}
