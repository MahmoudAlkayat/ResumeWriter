package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.service.SkillService;
import ninjas.cs490Project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillService skillService;
    private final UserRepository userRepository;

    public SkillController(SkillService skillService, UserRepository userRepository) {
        this.skillService = skillService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getUserSkills(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        return ResponseEntity.ok(skillService.getUserSkills(user));
    }

    @PostMapping
    public ResponseEntity<?> addSkill(@RequestBody Map<String, String> request, Authentication authentication) {
        String skillName = request.get("name");
        if (skillName == null || skillName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Skill name is required");
        }

        User user = userRepository.findByEmail(authentication.getName());
        Skill skill = skillService.addSkill(skillName, user);
        return ResponseEntity.ok(skill);
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<?> deleteSkill(@PathVariable Long skillId, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName());
            skillService.deleteSkill(skillId, user);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Batch add skills endpoint
    @PostMapping("/batch")
    public ResponseEntity<?> addSkills(@RequestBody Map<String, List<String>> request, Authentication authentication) {
        List<String> skillNames = request.get("skills");
        if (skillNames == null || skillNames.isEmpty()) {
            return ResponseEntity.badRequest().body("Skills list is required");
        }

        User user = userRepository.findByEmail(authentication.getName());
        List<Skill> skills = skillService.addSkills(skillNames, user);
        return ResponseEntity.ok(skills);
    }
} 