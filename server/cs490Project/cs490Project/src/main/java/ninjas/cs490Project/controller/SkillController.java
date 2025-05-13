package ninjas.cs490Project.controller;

import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    @Autowired
    private SkillService skillService;

    // DTO for Skill responses
    public static class SkillDTO {
        private Long id;
        private String name;

        public SkillDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @GetMapping
    public ResponseEntity<List<SkillDTO>> getUserSkills(@AuthenticationPrincipal User user) {
        List<Skill> skills = skillService.getUserSkills(user);
        List<SkillDTO> skillDTOs = skills.stream()
            .map(skill -> new SkillDTO(skill.getId(), skill.getName()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(skillDTOs);
    }

    @PostMapping
    public ResponseEntity<SkillDTO> addSkill(@AuthenticationPrincipal User user, @RequestBody Map<String, String> request) {
        String skillName = request.get("name");
        if (skillName == null || skillName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Skill skill = skillService.addSkill(skillName, user);
        return ResponseEntity.ok(new SkillDTO(skill.getId(), skill.getName()));
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<?> deleteSkill(@AuthenticationPrincipal User user, @PathVariable Long skillId) {
        skillService.deleteSkill(skillId, user);
        return ResponseEntity.ok().build();
    }

    // Batch add skills endpoint
    @PostMapping("/batch")
    public ResponseEntity<List<SkillDTO>> addSkills(@RequestBody Map<String, List<String>> request, @AuthenticationPrincipal User user) {
        List<String> skillNames = request.get("skills");
        if (skillNames == null || skillNames.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Skill> skills = skillService.addSkills(skillNames, user);
        List<SkillDTO> skillDTOs = skills.stream()
            .map(skill -> new SkillDTO(skill.getId(), skill.getName()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(skillDTOs);
    }
}
