package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Profile;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.ProfileRepository;
import ninjas.cs490Project.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        Profile profile = profileRepository.findByUser(user);
        
        if (profile == null) {
            // Create a new profile if one doesn't exist
            Profile newProfile = new Profile();
            newProfile.setUser(user);
            profile = profileRepository.save(newProfile);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("phone", profile.getPhone());
        response.put("address", profile.getAddress());
        response.put("themePreference", profile.getThemePreference());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getUsername());

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileUpdate,
            @AuthenticationPrincipal User user) {
        try {
            Profile existingProfile = profileRepository.findByUser(user);
            
            if (existingProfile == null) {
                // Create new profile if it doesn't exist
                Profile newProfile = new Profile();
                newProfile.setUser(user);
                existingProfile = profileRepository.save(newProfile);
            }

            // Update user fields
            if (profileUpdate.containsKey("firstName")) {
                user.setFirstName(profileUpdate.get("firstName"));
            }
            if (profileUpdate.containsKey("lastName")) {
                user.setLastName(profileUpdate.get("lastName"));
            }
            if (profileUpdate.containsKey("email")) {
                // Only update email if it's different from current
                String newEmail = profileUpdate.get("email");
                if (!newEmail.equals(user.getUsername())) {
                    user.setUsername(newEmail);
                }
            }
            userRepository.save(user);

            // Update profile fields
            if (profileUpdate.containsKey("phone")) {
                existingProfile.setPhone(profileUpdate.get("phone"));
            }
            if (profileUpdate.containsKey("address")) {
                existingProfile.setAddress(profileUpdate.get("address"));
            }
            if (profileUpdate.containsKey("themePreference")) {
                existingProfile.setThemePreference(profileUpdate.get("themePreference"));
            }
            existingProfile = profileRepository.save(existingProfile);

            Map<String, Object> response = new HashMap<>();
            response.put("phone", existingProfile.getPhone());
            response.put("address", existingProfile.getAddress());
            response.put("themePreference", existingProfile.getThemePreference());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getUsername());

            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            // Check if the error is due to unique constraint violation
            if (e.getMessage().contains("username")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "This email address is already in use. Please use a different email.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            // For other database integrity violations
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred while updating your profile. Please try again.");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred. Please try again.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 