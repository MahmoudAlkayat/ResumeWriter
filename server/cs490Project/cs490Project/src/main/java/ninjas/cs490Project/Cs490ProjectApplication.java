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

	}
}
