package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.RegistrationRequest;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

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
        user.setPasswordHash(request.getPassword());
        user.setIsVerified(false);

        // Save the user in the database
        userRepository.save(user);

        // Send the verification email (implementation hidden in the service)
        try {
            emailVerificationService.createVerificationTokenForUser(user);
        } catch (Exception e) {
            // Log the exception if needed
            System.err.println("Error sending verification email: " + e.getMessage());
            // Do not return an error response; the user was created successfully.
        }
        return ResponseEntity.ok("User registered successfully! Please check your email to verify your account.");
    }
}
