package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Integer> {
    // Retrieves all work experiences for a user by traversing through the resume relationship.
    List<WorkExperience> findByResumeUserId(int userId);
}
