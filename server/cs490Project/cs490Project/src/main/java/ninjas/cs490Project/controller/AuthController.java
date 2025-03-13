package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.EmailVerificationToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.EmailVerificationTokenRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return "Invalid verification token.";
        }
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Token has expired. Please request a new verification email.";
        }
        // Mark user as verified
        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        // Optionally delete the token to prevent reuse
        tokenRepository.delete(verificationToken);

        return "Your account has been verified!";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Find the user by email
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !user.getPasswordHash().equals(request.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate a JWT token
        String token = jwtService.generateToken(user);

        // Create an HTTP-only cookie with the token
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                // Set secure(true) when using HTTPS in production
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60) // token valid for 1 day
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Login successful");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromCookies(request);
        if (token != null && jwtService.validateToken(token)) {
            String email = jwtService.extractUsername(token);
            // You could load and return full user details here if desired
            return ResponseEntity.ok("Authenticated as " + email);
        } else {
            return ResponseEntity.status(401).body("Not authenticated");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie emptyCookie = ResponseCookie.from("jwt", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, emptyCookie.toString())
                .body("Logged out");
    }

    // Helper method to extract the JWT from cookies
    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    // Simple DTO for login requests
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
