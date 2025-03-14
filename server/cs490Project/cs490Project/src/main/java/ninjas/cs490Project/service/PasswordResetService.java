package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.PasswordResetToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.PasswordResetTokenRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

        // Check if a token already exists for this user
        PasswordResetToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) {
            // Optionally update the token and expiry, or delete it
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

}