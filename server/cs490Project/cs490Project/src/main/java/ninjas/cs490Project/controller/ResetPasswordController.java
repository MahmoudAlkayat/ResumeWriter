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
            return ResponseEntity.badRequest().body("Invalid or expired reset token.");
        }
        // Get the associated user and update the password (hashing it with BCrypt)
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Delete the token so it cannot be used again
        tokenRepository.delete(resetToken);
        return ResponseEntity.ok("Password reset successfully. Please log in with your new password.");
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
