package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.PasswordResetToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.PasswordResetTokenRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String createToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public PasswordResetToken createPasswordResetTokenForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("No user found with that email");
        }

        // Check if a token already exists for this user; if so, delete it.
        PasswordResetToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) {
            tokenRepository.delete(existingToken);
        }

        String token = createToken();
        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        PasswordResetToken savedToken = tokenRepository.save(myToken);

        // Send email (or simulate sending email)
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        mailService.sendResetLink(user.getEmail(), resetLink);

        return savedToken;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired token");
        }
        User user = resetToken.getUser();

        // Check if newPassword is the same as the current password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as the old password");
        }

        // Enforce a simple password policy: at least 8 characters, at least one uppercase letter, and one digit.
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("Password must be at least 8 characters long, contain at least one uppercase letter and one digit");
        }

        // Update the user's password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate the token after a successful reset
        tokenRepository.delete(resetToken);
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
}