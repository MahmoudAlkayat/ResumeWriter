package ninjas.cs490Project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ninjas.cs490Project.entity.JobDescription;
import ninjas.cs490Project.entity.User;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByUserId(Long userId);
    List<JobDescription> findByUserOrderByCreatedAtDesc(User user);
}