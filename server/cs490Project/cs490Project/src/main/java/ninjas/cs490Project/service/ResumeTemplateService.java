package ninjas.cs490Project.service;

import ninjas.cs490Project.entity.ResumeTemplate;
import ninjas.cs490Project.repository.ResumeTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResumeTemplateService {
    private final ResumeTemplateRepository templateRepository;

    public ResumeTemplateService(ResumeTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<ResumeTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ResumeTemplate> getTemplatesByFormatType(String formatType) {
        return templateRepository.findByFormatType(formatType);
    }

    @Transactional(readOnly = true)
    public ResumeTemplate getTemplateById(String templateId) {
        return templateRepository.findByTemplateId(templateId);
    }

    @Transactional
    public ResumeTemplate createTemplate(ResumeTemplate template) {
        return templateRepository.save(template);
    }
}