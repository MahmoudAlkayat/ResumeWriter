package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    // Custom queries can be defined here, e.g.:
    // List<WorkExperience> findByCompany(String company);
}