package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Integer> {
    @Query("SELECT e FROM Education e WHERE e.resume.user.id = :userId")
    List<Education> findByResumeUserId(@Param("userId") int userId);
}
