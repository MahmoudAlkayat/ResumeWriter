package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.UploadedResume;
import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UploadedResumeRepository extends JpaRepository<UploadedResume, Long> {
    List<UploadedResume> findByUser(User user);
} 