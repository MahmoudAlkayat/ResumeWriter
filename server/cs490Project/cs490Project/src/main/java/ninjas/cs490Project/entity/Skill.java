package ninjas.cs490Project.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;  // <--- field is called "name"

    @ManyToMany(mappedBy = "skills")
    private Set<Resume> resumes;

    public int getId() {
        return id;
    }

    // If needed
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    // IMPORTANT: define setName to match usage
    public void setName(String name) {
        this.name = name;
    }

    public Set<Resume> getResumes() {
        return resumes;
    }

    public void setResumes(Set<Resume> resumes) {
        this.resumes = resumes;
    }
}
