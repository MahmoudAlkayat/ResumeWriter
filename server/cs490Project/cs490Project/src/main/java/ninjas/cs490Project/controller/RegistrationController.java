package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.RegistrationRequest;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        // Basic validation for required fields
        if (request.getFirstName() == null || request.getFirstName().isEmpty() ||
                request.getLastName() == null || request.getLastName().isEmpty() ||
                request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        // Check if email is already in use
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email already in use.");
        }

        // Create the user with verified flag set to false
        User user = new User();
        user.setUsername(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        // Hash the plain-text password using BCrypt
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsVerified(false);

        // Save the user in the database
        userRepository.save(user);

        // Send the verification email (if there's an error sending, we log it but still return success)
        try {
            emailVerificationService.createVerificationTokenForUser(user);
        } catch (Exception e) {
            System.err.println("Error sending verification email: " + e.getMessage());
        }

        return ResponseEntity.ok("User registered successfully! Please check your email to verify your account.");
    }
}
