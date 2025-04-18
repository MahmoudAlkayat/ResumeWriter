package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Profile;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.ProfileRepository;
import ninjas.cs490Project.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        
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

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody Profile updatedProfile,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        
        Profile existingProfile = profileRepository.findByUser(user);
        
        if (existingProfile == null) {
            // Create new profile if it doesn't exist
            updatedProfile.setUser(user);
            existingProfile = profileRepository.save(updatedProfile);
        } else {
            // Update existing profile
            if (updatedProfile.getPhone() != null) {
                existingProfile.setPhone(updatedProfile.getPhone());
            }
            if (updatedProfile.getAddress() != null) {
                existingProfile.setAddress(updatedProfile.getAddress());
            }
            if (updatedProfile.getThemePreference() != null) {
                existingProfile.setThemePreference(updatedProfile.getThemePreference());
            }
            existingProfile = profileRepository.save(existingProfile);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("phone", existingProfile.getPhone());
        response.put("address", existingProfile.getAddress());
        response.put("themePreference", existingProfile.getThemePreference());

        return ResponseEntity.ok(response);
    }
} 