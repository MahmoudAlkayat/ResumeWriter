package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.FreeformEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeformEntryRepository extends JpaRepository<FreeformEntry, Integer> {
    List<FreeformEntry> findByUserId(Integer userId);
} 