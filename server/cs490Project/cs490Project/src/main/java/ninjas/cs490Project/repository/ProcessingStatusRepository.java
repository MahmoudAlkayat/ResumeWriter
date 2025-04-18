package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingStatusRepository extends JpaRepository<ProcessingStatus, Long> {
} 