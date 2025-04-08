package ninjas.cs490Project.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import ninjas.cs490Project.dto.GoogleOAuthUser;
import ninjas.cs490Project.entity.Profile;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.ProfileRepository;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JWTService;
import ninjas.cs490Project.service.oauth.GoogleOAuthService;
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
@RequestMapping("/oauth/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/callback")
    public void handleGoogleCallback(@RequestParam(value = "code", required = false) String code,
                                     @RequestParam(value = "error", required = false) String error,
                                     HttpServletResponse response) throws IOException {
        // If there is an error parameter (e.g., user canceled), redirect to login
        if (error != null) {
            response.sendRedirect("http://localhost:3000/login?oauth=canceled");
            return;
        }
        // If code is missing, redirect with error
        if (code == null) {
            response.sendRedirect("http://localhost:3000/login?oauth=error");
            return;
        }

        try {
            // 1) Exchange the authorization code for tokens
            GoogleTokenResponse tokenResponse = googleOAuthService.exchangeCodeForTokens(code);

            // 2) Verify the ID token and extract user info
            GoogleOAuthUser googleUser = googleOAuthService.getUserFromIdToken(tokenResponse.getIdToken());

            // 3) Check if the user exists in the database; if not, create a new user.
            User user = userRepository.findByEmail(googleUser.getEmail());
            if (user == null) {
                user = new User();
                user.setEmail(googleUser.getEmail());
                user.setUsername(googleUser.getEmail()); // or your own logic
                user.setFirstName(googleUser.getFirstName() != null ? googleUser.getFirstName() : "");
                user.setLastName(googleUser.getLastName() != null ? googleUser.getLastName() : "");
                user.setIsVerified(true);

                // Generate a random password and hash it.
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

            // 4) Optionally generate a local JWT for your session
            String jwt = jwtService.generateToken(user);

            // 5) Create an HttpOnly cookie with the JWT
            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .secure(false) // set true in production with HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60) // token valid for 1 day
                    .build();

            // 6) Add the cookie to the response headers
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 7) Add user data to redirect URL
            String userData = String.format("id=%s&email=%s&firstName=%s&lastName=%s&themePreference=%s",
                URLEncoder.encode(String.valueOf(user.getId()), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(user.getFirstName(), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(user.getLastName(), StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(themePreference, StandardCharsets.UTF_8.toString())
            );

            // 8) Redirect with user data
            response.sendRedirect("http://localhost:3000/auth-success?oauth=google&" + userData);
        } catch (Exception e) {
            e.printStackTrace();
            // If there's an error, redirect to login with an error parameter.
            response.sendRedirect("http://localhost:3000/login?oauth=error");
        }
    }
}