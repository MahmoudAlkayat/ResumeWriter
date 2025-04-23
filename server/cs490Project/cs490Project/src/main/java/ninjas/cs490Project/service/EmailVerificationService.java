package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.EmailVerificationToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private MailService mailService;

    public String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public EmailVerificationToken createVerificationTokenForUser(User user){
        // Delete existing token if exists
        EmailVerificationToken existing = tokenRepository.findByUser(user);
        if(existing != null){
            tokenRepository.delete(existing);
        }
        // Generate new token
        String token = generateToken();

        // Create and save Entity
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        //Sending verification email
        String verifylink = "http://localhost:8080/auth/verify-email?token=" + token;
        mailService.sendVerificationEmail(user.getEmail(),verifylink);

        return verificationToken;
    }
}