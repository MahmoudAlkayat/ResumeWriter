package ninjas.cs490Project;

import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.repository.EducationRepository;
import ninjas.cs490Project.repository.ResumeRepository;
import ninjas.cs490Project.repository.PasswordResetTokenRepository;
import ninjas.cs490Project.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class Cs490ProjectApplication implements CommandLineRunner {

	@Autowired
	private EducationRepository educationRepository;

	@Autowired
	private ResumeRepository resumeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordResetService passwordResetService;

	@Autowired
	private PasswordResetTokenRepository tokenRepository;

	public static void main(String[] args) {
		SpringApplication.run(Cs490ProjectApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// --- Step 1: Create a new User with an initial password.
		User user = userRepository.findByEmail("belal919@live.com");
		if (user == null) {
			user = new User();
			user.setUsername("testuser1");
			user.setFirstName("Test");
			user.setLastName("User");
			user.setEmail("testuser1@example.com");
			user.setPasswordHash("initialHashedPassword");
			user = userRepository.save(user);
			System.out.println("Created user with password: " + user.getPasswordHash());
		} else {
			System.out.println("User already exists with password: " + user.getPasswordHash());
		}


		// --- Step 2: Create a reset token and send an email (simulated)
		PasswordResetToken resetToken = passwordResetService.createPasswordResetTokenForUser(user.getEmail());
		System.out.println("Reset token created: " + resetToken.getToken());


		// --- Step 3: Retrieve and verify the token is in the database.
		PasswordResetToken retrievedToken = tokenRepository.findByToken(resetToken.getToken());
		if (retrievedToken != null && retrievedToken.getExpiryDate().isAfter(LocalDateTime.now())) {
			System.out.println("Token is valid and stored in the database.");
		} else {
			System.out.println("Token is invalid or expired.");
		}
	}
}
