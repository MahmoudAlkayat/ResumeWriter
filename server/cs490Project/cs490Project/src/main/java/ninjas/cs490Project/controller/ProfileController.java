package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Profile;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.ProfileRepository;
import ninjas.cs490Project.repository.UserRepository;
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

    private class ProfileResponse {
        private String phone;
        private String address;
        private String themePreference;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getThemePreference() {
            return themePreference;
        }

        public void setThemePreference(String themePreference) {
            this.themePreference = themePreference;
        }
    }

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

        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setPhone(profile.getPhone());
        profileResponse.setAddress(profile.getAddress());
        profileResponse.setThemePreference(profile.getThemePreference());

        return ResponseEntity.ok(profileResponse);
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

        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setPhone(existingProfile.getPhone());
        profileResponse.setAddress(existingProfile.getAddress());
        profileResponse.setThemePreference(existingProfile.getThemePreference());

        return ResponseEntity.ok(profileResponse);
    }
} 