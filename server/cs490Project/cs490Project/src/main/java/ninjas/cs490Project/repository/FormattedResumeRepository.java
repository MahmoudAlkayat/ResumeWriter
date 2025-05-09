package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.FormattedResume;
import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormattedResumeRepository extends JpaRepository<FormattedResume, Long> {
    List<FormattedResume> findByUser(User user);
} 