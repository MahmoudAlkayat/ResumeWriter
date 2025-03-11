// src/main/java/ninjas/cs490Project/controller/RegistrationController.java
package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.RegistrationRequest;
import ninjas.cs490Project.entity.EmailVerificationToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")  // or "/auth" - your choice
public class RegistrationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        // Basic validation (could be more robust)
        if (request.getFirstName() == null || request.getFirstName().isEmpty() ||
                request.getLastName() == null || request.getLastName().isEmpty() ||
                request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        // Check if email is already used
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email already in use.");
        }

        // Create and save the user
        User user = new User();
        user.setUsername(request.getEmail());  // or any other logic for username
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        // For demonstration, store the password in plain text or hashed
        user.setPasswordHash(request.getPassword());
        user.setIsVerified(false);

        userRepository.save(user);

        // send verification link
        emailVerificationService.createVerificationTokenForUser(user);


        return ResponseEntity.ok("User registered successfully!");
    }
}
