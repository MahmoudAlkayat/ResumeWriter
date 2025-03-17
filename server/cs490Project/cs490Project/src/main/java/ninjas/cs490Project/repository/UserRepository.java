package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    // Added method to retrieve a user by their id
    User findUserById(int id);
}
