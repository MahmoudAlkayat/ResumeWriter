package ninjas.cs490Project.controller;

import ninjas.cs490Project.PasswordResetToken;
import ninjas.cs490Project.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class ForgotPasswordController {

    @Autowired
    private PasswordResetService passwordResetService;

    // This endpoint initiates the forgot-password process
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        try {
            PasswordResetToken token = passwordResetService.createPasswordResetTokenForUser(email);
            // For testing, we return the token. In production, you wouldn't return the token in the response.
            return ResponseEntity.ok("Reset token has been sent to your email. (Token: " + token.getToken() + ")");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
