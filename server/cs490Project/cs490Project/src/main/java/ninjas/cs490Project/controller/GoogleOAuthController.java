package ninjas.cs490Project.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import ninjas.cs490Project.dto.GoogleOAuthUser;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.UserRepository;
import ninjas.cs490Project.service.JWTService;
import ninjas.cs490Project.service.oauth.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/oauth/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/callback")
    public void handleGoogleCallback(@RequestParam("code") String code,
                                     HttpServletResponse response) throws IOException {
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

            // 4) Optionally generate a local JWT for your session
            String jwt = jwtService.generateToken(user);

            // 5) Create an HttpOnly cookie with the JWT
            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .secure(false)    // set true in production with HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60) // 1 day
                    .build();

            // 6) Add the cookie to the response headers
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 7) Redirect to your front-end home page
            response.sendRedirect("http://localhost:3000/home");
        } catch (Exception e) {
            e.printStackTrace();
            // If there's an error, redirect or respond as needed
            response.sendRedirect("http://localhost:3000/login?oauth=error");
        }
    }
}
