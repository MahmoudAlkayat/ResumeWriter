package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    // Example custom query method (uncomment if needed):
    // List<Education> findByInstitution(String institution);
}
