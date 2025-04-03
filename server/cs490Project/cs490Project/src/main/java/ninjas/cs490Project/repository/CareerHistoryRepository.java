package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.CareerHistory;
import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerHistoryRepository extends JpaRepository<CareerHistory, String> {
    List<CareerHistory> findByUser(User user);
}
