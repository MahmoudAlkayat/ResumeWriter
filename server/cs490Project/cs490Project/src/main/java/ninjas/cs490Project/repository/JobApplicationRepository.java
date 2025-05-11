package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.JobApplication;
import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserOrderByAppliedAtDesc(User user);
    boolean existsByResumeIdAndJobId(Long resumeId, Long jobId);
} 