package ninjas.cs490Project.controller;

import ninjas.cs490Project.dto.CareerHistoryRequest;
import ninjas.cs490Project.dto.CareerHistoryResponse;
import ninjas.cs490Project.entity.CareerHistory;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.CareerHistoryRepository;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class CareerHistoryController {

    @Autowired
    private CareerHistoryRepository careerHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<?> getCareerHistory(Authentication authentication) {
        // Get the authenticated user
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Get all career history entries for the user
        List<CareerHistory> histories = careerHistoryRepository.findByUser(user);
        
        return ResponseEntity.ok(new CareerHistoryResponse(
            null,
            "success",
            histories
        ));
    }

    @PostMapping("/history")
    public ResponseEntity<CareerHistoryResponse> submitCareerHistory(
            @RequestBody CareerHistoryRequest request,
            Authentication authentication) {
        
        // Get the authenticated user
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Create and save the career history
        CareerHistory careerHistory = new CareerHistory();
        careerHistory.setRawText(request.getText());
        careerHistory.setUser(user);
        
        CareerHistory savedHistory = careerHistoryRepository.save(careerHistory);

        // Return the response
        return ResponseEntity.ok(new CareerHistoryResponse(
                savedHistory.getId(),
                "saved"
        ));
    }
}
