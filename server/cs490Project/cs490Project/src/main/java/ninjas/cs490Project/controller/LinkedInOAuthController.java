package ninjas.cs490Project.controller;

import jakarta.servlet.http.HttpServletResponse;
import ninjas.cs490Project.dto.LinkedInOAuthUser;
import ninjas.cs490Project.entity.Profile;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.ProfileRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JWTService;
import ninjas.cs490Project.service.oauth.LinkedInOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/oauth/linkedin")
public class LinkedInOAuthController {

    @Autowired
    private LinkedInOAuthService linkedInOAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/callback")
    public void handleLinkedInCallback(@RequestParam(value = "code", required = false) String code,
                                     @RequestParam(value = "error", required = false) String error,
                                     HttpServletResponse response) throws IOException {
        if (error != null) {
            response.sendRedirect("http://localhost:3000/login?oauth=cancelled");
            return;
        }
        if (code == null) {
            response.sendRedirect("http://localhost:3000/login?oauth=error");
            return;
        }

        try {
            // Exchange code for access token
            String accessToken = linkedInOAuthService.exchangeCodeForToken(code);

            // Get user info using the access token
            LinkedInOAuthUser linkedInUser = linkedInOAuthService.getUserInfo(accessToken);

            // Find or create user
            User user = userRepository.findByEmail(linkedInUser.getEmail());
            if (user == null) {
                user = new User();
                user.setEmail(linkedInUser.getEmail());
                user.setUsername(linkedInUser.getEmail());
                user.setFirstName(linkedInUser.getFirstName());
                user.setLastName(linkedInUser.getLastName());
                user.setIsVerified(true);

                String randomPassword = UUID.randomUUID().toString();
                user.setPasswordHash(passwordEncoder.encode(randomPassword));

                userRepository.save(user);
            }

            String themePreference;
            Profile profile = profileRepository.findByUser(user);
            if (profile != null && profile.getThemePreference() != null) {
                themePreference = profile.getThemePreference();
            } else {
                themePreference = "light";
            }

            // Generate JWT and set cookie
            String jwt = jwtService.generateToken(user);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            
            // Create URL-encoded user data
            String userData = String.format("id=%s&email=%s&firstName=%s&lastName=%s",
                URLEncoder.encode(String.valueOf(user.getId()), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(user.getFirstName(), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(user.getLastName(), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(themePreference, StandardCharsets.UTF_8.toString())
            );
            
            response.sendRedirect("http://localhost:3000/auth-success?oauth=linkedin&" + userData);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/login?oauth=error");
        }
    }
} 