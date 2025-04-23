package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.Skill;
import ninjas.cs490Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    Skill findByNameAndUser(String name, User user);
    List<Skill> findByUser(User user);
    Optional<Skill> findByNameIgnoreCaseAndUser(String name, User user);
}
