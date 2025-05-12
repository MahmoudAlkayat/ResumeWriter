package ninjas.cs490Project.repository;

import ninjas.cs490Project.entity.ResumeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeTemplateRepository extends JpaRepository<ResumeTemplate, Long> {
    List<ResumeTemplate> findByFormatType(String formatType);
    ResumeTemplate findByTemplateId(String templateId);
}