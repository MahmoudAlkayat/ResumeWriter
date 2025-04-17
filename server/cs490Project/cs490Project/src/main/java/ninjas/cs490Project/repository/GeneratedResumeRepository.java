package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, Long> {
    List<GeneratedResume> findByUser(User user);
    List<GeneratedResume> findByUserAndJobDescriptionId(User user, Long jobDescriptionId);
} 