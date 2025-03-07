package ninjas.cs490Project.repository;

import ninjas.cs490Project.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    // Custom queries can be defined here, e.g.:
    // List<Resume> findByTitle(String title);
}
