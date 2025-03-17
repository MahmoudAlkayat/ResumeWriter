package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.JobListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobListingRepository extends JpaRepository<JobListing, Long> {
    // You could add custom queries here if needed.
}
