package ninjas.cs490Project.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ninjas.cs490Project.entity.EmailVerificationToken;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.EmailVerificationTokenRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private EmailVerificationTokenRepository tokenRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JWTService jwtService;


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            response.sendRedirect("http://localhost:3000/login?verification=invalid");
            return;
        }
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            response.sendRedirect("http://localhost:3000/login?verification=expired");
            return;
        }
        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        response.sendRedirect("http://localhost:3000/login?verified=true");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Find the user by email
        User user = userRepository.findByEmail(request.getEmail());
        // Use BCrypt to verify the password
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        // Check if the user's email has been verified
        if (user.getIsVerified() == null || !user.getIsVerified()) {
            return ResponseEntity.status(403).body("Please verify your email before logging in.");
        }
        // Generate a JWT token
        String token = jwtService.generateToken(user);
        // Create an HTTP-only cookie with the token
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // Set true when using HTTPS in production
                .path("/")
                .maxAge(24 * 60 * 60) // token valid for 1 day
                .build();

        Map<String, String> userData = new HashMap<>();
        userData.put("id", String.valueOf(user.getId()));
        userData.put("email", user.getEmail());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userData);
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromCookies(request);
        if (token != null && jwtService.validateToken(token)) {
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email);
            Map <String, String> userData = new HashMap<>();
            userData.put("id", String.valueOf(user.getId()));
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            return ResponseEntity.ok(userData);
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


    // DTO for login requests
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

