package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.ProcessingStatus;
import ninjas.cs490Project.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingStatusRepository extends JpaRepository<ProcessingStatus, Long> {
    List<ProcessingStatus> findByUserOrderByStartedAtDesc(User user, Pageable pageable);
} 