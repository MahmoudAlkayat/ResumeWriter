package ninjas.cs490Project;

import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.repository.EmailVerificationTokenRepository;
import ninjas.cs490Project.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
public class Cs490ProjectApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailVerificationService emailVerificationService;

	@Autowired
	private EmailVerificationTokenRepository verificationTokenRepository;

	public static void main(String[] args) {
		SpringApplication.run(Cs490ProjectApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		// 1. Find or create a test user
		String testEmail = "cs490ninjas@gmail.com";
		User user = userRepository.findByEmail(testEmail);
		if (user == null) {
			user = new User();
			user.setUsername(testEmail);
			user.setFirstName("cs490");
			user.setLastName("Ninjas");
			user.setEmail(testEmail);
			user.setPasswordHash("someHashedPassword");
			user.setIsVerified(false);
			user = userRepository.save(user);
			System.out.println("Created new user with isVerified = false");
		} else {
			System.out.println("User already exists. Current isVerified = " + user.getIsVerified());
		}

		// 2. Generate a verification token and simulate sending email
		emailVerificationService.createVerificationTokenForUser(user);
		System.out.println("Verification token created. Check logs or console for the link.");

		// (Optional) 3. Demonstrate retrieving the token from the repository:
		// (You might do this if you want to simulate verifying it here in CommandLineRunner.)
		var existingToken = verificationTokenRepository.findByUser(user);
		if (existingToken != null && existingToken.getExpiryDate().isAfter(LocalDateTime.now())) {
			System.out.println("Verification token found in DB: " + existingToken.getToken());
		} else {
			System.out.println("No valid verification token found for user.");
		}

		// (Optional) 4. If you want to simulate verifying the user right away:
		// user.setIsVerified(true);
		// userRepository.save(user);
		// verificationTokenRepository.delete(existingToken);
		// System.out.println("User is now verified. isVerified = " + user.getIsVerified());
	}
}
