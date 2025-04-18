package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SkillService {
    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> getUserSkills(User user) {
        return skillRepository.findByUser(user);
    }

    @Transactional
    public Skill addSkill(String skillName, User user) {
        // Normalize skill name (trim and convert to lowercase for comparison)
        String normalizedSkillName = normalizeSkillName(skillName);
        
        // Check if skill already exists for this user (case-insensitive)
        Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCaseAndUser(normalizedSkillName, user);
        
        if (existingSkill.isPresent()) {
            return existingSkill.get();
        }

        // Create new skill if it doesn't exist
        Skill newSkill = new Skill();
        newSkill.setName(skillName.trim()); // Preserve original case for display
        newSkill.setUser(user);
        return skillRepository.save(newSkill);
    }

    @Transactional
    public void deleteSkill(Long skillId, User user) {
        Optional<Skill> skill = skillRepository.findById(skillId);
        if (skill.isPresent() && skill.get().getUser().getId() == user.getId()) {
            skillRepository.delete(skill.get());
        } else {
            throw new IllegalArgumentException("Skill not found or unauthorized");
        }
    }

    @Transactional
    public List<Skill> addSkills(List<String> skillNames, User user) {
        return skillNames.stream()
                .map(skillName -> addSkill(skillName, user))
                .toList();
    }

    private String normalizeSkillName(String skillName) {
        if (skillName == null) {
            throw new IllegalArgumentException("Skill name cannot be null");
        }
        return skillName.trim().toLowerCase();
    }
} 