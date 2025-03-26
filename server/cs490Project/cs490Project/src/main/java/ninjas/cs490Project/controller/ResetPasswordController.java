package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.PasswordResetToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.PasswordResetTokenRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class ResetPasswordController {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Endpoint to update the user's password using the token.
     * Expects a request parameter "token" and a JSON body with the new password.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @RequestBody ResetPasswordRequest request) {
        // Find the reset token
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token. Please request a new reset-password email!");
        }
        // Get the associated user and update the password (hashing it with BCrypt)
        User user = resetToken.getUser();
        // Optionally, enforce password policies and prevent reuse of the old password here.
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("New password cannot be the same as the old password.");
        }
        // Enforce a simple password policy (e.g., minimum length, uppercase letter, digit, etc.)
        if (!isValidPassword(request.getNewPassword())) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long and contain at least one uppercase letter and one digit.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Delete the token so it cannot be used again
        tokenRepository.delete(resetToken);
        return ResponseEntity.ok("Password reset successfully. Please log in with your new password.");
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            return false;
        }
        return true;
    }

    // DTO for the new password request
    public static class ResetPasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}