package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    // Custom queries can be defined here, e.g.:
    // Optional<Skill> findByName(String name);
}