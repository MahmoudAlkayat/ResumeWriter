package ninjas.cs490Project.repository;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {
    EmailVerificationToken findByToken(String token);
    EmailVerificationToken findByUser(User user);
}