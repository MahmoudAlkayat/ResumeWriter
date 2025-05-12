package ninjas.cs490Project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninjas.cs490Project.dto.ResumeGenerationResult;
import ninjas.cs490Project.entity.FormattedResume;
import ninjas.cs490Project.entity.GeneratedResume;
import ninjas.cs490Project.entity.ResumeTemplate;
import ninjas.cs490Project.entity.User;
import ninjas.cs490Project.repository.FormattedResumeRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

@Service
public class ResumeFormattingService {
    private final FormattedResumeRepository formattedResumeRepository;
    private final ObjectMapper objectMapper;
    private final ResumeTemplateService templateService;

    public ResumeFormattingService(FormattedResumeRepository formattedResumeRepository, 
                                 ObjectMapper objectMapper,
                                 ResumeTemplateService templateService) {
        this.formattedResumeRepository = formattedResumeRepository;
        this.objectMapper = objectMapper;
        this.templateService = templateService;
    }

    public FormattedResume formatResume(GeneratedResume generatedResume, String formatType, User user, String templateId) throws Exception {
        // Parse the JSON content
        ResumeGenerationResult resumeData = objectMapper.readValue(generatedResume.getContent(), ResumeGenerationResult.class);
        
        // Format the resume based on the requested format
        String formattedContent;
        String fileExtension;
        
        switch (formatType.toLowerCase()) {
            case "markdown":
                formattedContent = formatAsMarkdown(resumeData);
                fileExtension = "md";
                break;
            case "html":
                formattedContent = formatAsHtml(resumeData);
                fileExtension = "html";
                break;
            case "text":
                formattedContent = formatAsText(resumeData);
                fileExtension = "txt";
                break;
            case "latex":
                ResumeTemplate template = templateId != null ? 
                    templateService.getTemplateById(templateId) : 
                    templateService.getTemplateById("classic");
                formattedContent = formatAsLatex(resumeData, template);
                fileExtension = "tex";
                break;
            default:
                throw new IllegalArgumentException("Unsupported format type: " + formatType);
        }

        // Create and save the formatted resume
        FormattedResume formattedResume = new FormattedResume();
        formattedResume.setContent(formattedContent);
        formattedResume.setFormatType(formatType);
        formattedResume.setFileExtension(fileExtension);
        formattedResume.setCreatedAt(Instant.now());
        formattedResume.setGeneratedResume(generatedResume);
        formattedResume.setUser(user);

        return formattedResumeRepository.save(formattedResume);
    }

    private String formatAsMarkdown(ResumeGenerationResult resumeData) {
        StringBuilder markdown = new StringBuilder();
        
        // Personal Info
        markdown.append("# ").append(resumeData.getPersonalInfo().getFirstName())
               .append(" ").append(resumeData.getPersonalInfo().getLastName()).append("\n\n");
        
        if (resumeData.getPersonalInfo().getEmail() != null) {
            markdown.append("Email: ").append(resumeData.getPersonalInfo().getEmail()).append("\n");
        }
        if (resumeData.getPersonalInfo().getPhone() != null) {
            markdown.append("Phone: ").append(resumeData.getPersonalInfo().getPhone()).append("\n");
        }
        if (resumeData.getPersonalInfo().getAddress() != null) {
            markdown.append("Address: ").append(resumeData.getPersonalInfo().getAddress()).append("\n");
        }
        markdown.append("\n");

        // Skills
        if (resumeData.getSkills() != null && !resumeData.getSkills().isEmpty()) {
            markdown.append("## Skills\n");
            markdown.append(String.join(", ", resumeData.getSkills())).append("\n\n");
        }

        // Work Experience
        if (resumeData.getWorkExperienceList() != null && !resumeData.getWorkExperienceList().isEmpty()) {
            markdown.append("## Work Experience\n\n");
            for (var exp : resumeData.getWorkExperienceList()) {
                markdown.append("### ").append(exp.getJobTitle()).append(" at ").append(exp.getCompany()).append("\n");
                markdown.append(exp.getStartDate()).append(" - ").append(exp.getEndDate()).append("\n\n");
                
                if (exp.getResponsibilities() != null && !exp.getResponsibilities().isEmpty()) {
                    markdown.append("**Responsibilities:**\n").append(exp.getResponsibilities()).append("\n\n");
                }
                
                if (exp.getAccomplishments() != null && !exp.getAccomplishments().isEmpty()) {
                    markdown.append("**Accomplishments:**\n").append(exp.getAccomplishments()).append("\n\n");
                }
            }
        }

        // Education
        if (resumeData.getEducationList() != null && !resumeData.getEducationList().isEmpty()) {
            markdown.append("## Education\n\n");
            for (var edu : resumeData.getEducationList()) {
                markdown.append("### ").append(edu.getDegree()).append(" in ").append(edu.getFieldOfStudy()).append("\n");
                markdown.append(edu.getInstitution()).append("\n");
                markdown.append(edu.getStartDate()).append(" - ").append(edu.getEndDate()).append("\n");
                if (edu.getGpa() > 0) {
                    markdown.append("GPA: ").append(edu.getGpa()).append("\n");
                }
                if (edu.getDescription() != null && !edu.getDescription().isEmpty()) {
                    markdown.append(edu.getDescription()).append("\n");
                }
                markdown.append("\n");
            }
        }

        return markdown.toString();
    }

    private String formatAsHtml(ResumeGenerationResult resumeData) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; }\n")
            .append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n")
            .append("h2 { color: #2c3e50; margin-top: 20px; }\n")
            .append("h3 { color: #34495e; }\n")
            .append(".section { margin-bottom: 20px; }\n")
            .append(".contact-info { margin-bottom: 20px; }\n")
            .append(".skills { margin-bottom: 20px; }\n")
            .append(".experience-item, .education-item { margin-bottom: 15px; }\n")
            .append("</style>\n")
            .append("</head>\n<body>\n");

        // Personal Info
        html.append("<h1>").append(resumeData.getPersonalInfo().getFirstName())
            .append(" ").append(resumeData.getPersonalInfo().getLastName()).append("</h1>\n");
        
        html.append("<div class=\"contact-info\">\n");
        if (resumeData.getPersonalInfo().getEmail() != null) {
            html.append("<p>Email: ").append(resumeData.getPersonalInfo().getEmail()).append("</p>\n");
        }
        if (resumeData.getPersonalInfo().getPhone() != null) {
            html.append("<p>Phone: ").append(resumeData.getPersonalInfo().getPhone()).append("</p>\n");
        }
        if (resumeData.getPersonalInfo().getAddress() != null) {
            html.append("<p>Address: ").append(resumeData.getPersonalInfo().getAddress()).append("</p>\n");
        }
        html.append("</div>\n");

        // Skills
        if (resumeData.getSkills() != null && !resumeData.getSkills().isEmpty()) {
            html.append("<div class=\"section\">\n")
                .append("<h2>Skills</h2>\n")
                .append("<p>").append(String.join(", ", resumeData.getSkills())).append("</p>\n")
                .append("</div>\n");
        }

        // Work Experience
        if (resumeData.getWorkExperienceList() != null && !resumeData.getWorkExperienceList().isEmpty()) {
            html.append("<div class=\"section\">\n")
                .append("<h2>Work Experience</h2>\n");
            
            for (var exp : resumeData.getWorkExperienceList()) {
                html.append("<div class=\"experience-item\">\n")
                    .append("<h3>").append(exp.getJobTitle()).append(" at ").append(exp.getCompany()).append("</h3>\n")
                    .append("<p>").append(exp.getStartDate()).append(" - ").append(exp.getEndDate()).append("</p>\n");
                
                if (exp.getResponsibilities() != null && !exp.getResponsibilities().isEmpty()) {
                    html.append("<h4>Responsibilities:</h4>\n")
                        .append("<p>").append(exp.getResponsibilities()).append("</p>\n");
                }
                
                if (exp.getAccomplishments() != null && !exp.getAccomplishments().isEmpty()) {
                    html.append("<h4>Accomplishments:</h4>\n")
                        .append("<p>").append(exp.getAccomplishments()).append("</p>\n");
                }
                
                html.append("</div>\n");
            }
            html.append("</div>\n");
        }

        // Education
        if (resumeData.getEducationList() != null && !resumeData.getEducationList().isEmpty()) {
            html.append("<div class=\"section\">\n")
                .append("<h2>Education</h2>\n");
            
            for (var edu : resumeData.getEducationList()) {
                html.append("<div class=\"education-item\">\n")
                    .append("<h3>").append(edu.getDegree()).append(" in ").append(edu.getFieldOfStudy()).append("</h3>\n")
                    .append("<p>").append(edu.getInstitution()).append("</p>\n")
                    .append("<p>").append(edu.getStartDate()).append(" - ").append(edu.getEndDate()).append("</p>\n");
                
                if (edu.getGpa() > 0) {
                    html.append("<p>GPA: ").append(edu.getGpa()).append("</p>\n");
                }
                
                if (edu.getDescription() != null && !edu.getDescription().isEmpty()) {
                    html.append("<p>").append(edu.getDescription()).append("</p>\n");
                }
                
                html.append("</div>\n");
            }
            html.append("</div>\n");
        }

        html.append("</body>\n</html>");
        return html.toString();
    }

    private String formatAsText(ResumeGenerationResult resumeData) {
        StringBuilder text = new StringBuilder();
        
        // Personal Info
        text.append(resumeData.getPersonalInfo().getFirstName())
            .append(" ").append(resumeData.getPersonalInfo().getLastName()).append("\n\n");
        
        if (resumeData.getPersonalInfo().getEmail() != null) {
            text.append("Email: ").append(resumeData.getPersonalInfo().getEmail()).append("\n");
        }
        if (resumeData.getPersonalInfo().getPhone() != null) {
            text.append("Phone: ").append(resumeData.getPersonalInfo().getPhone()).append("\n");
        }
        if (resumeData.getPersonalInfo().getAddress() != null) {
            text.append("Address: ").append(resumeData.getPersonalInfo().getAddress()).append("\n");
        }
        text.append("\n");

        // Skills
        if (resumeData.getSkills() != null && !resumeData.getSkills().isEmpty()) {
            text.append("SKILLS\n");
            text.append("------\n");
            text.append(String.join(", ", resumeData.getSkills())).append("\n\n");
        }

        // Work Experience
        if (resumeData.getWorkExperienceList() != null && !resumeData.getWorkExperienceList().isEmpty()) {
            text.append("WORK EXPERIENCE\n");
            text.append("---------------\n\n");
            
            for (var exp : resumeData.getWorkExperienceList()) {
                text.append(exp.getJobTitle()).append(" at ").append(exp.getCompany()).append("\n");
                text.append(exp.getStartDate()).append(" - ").append(exp.getEndDate()).append("\n\n");
                
                if (exp.getResponsibilities() != null && !exp.getResponsibilities().isEmpty()) {
                    text.append("Responsibilities:\n").append(exp.getResponsibilities()).append("\n\n");
                }
                
                if (exp.getAccomplishments() != null && !exp.getAccomplishments().isEmpty()) {
                    text.append("Accomplishments:\n").append(exp.getAccomplishments()).append("\n\n");
                }
            }
        }

        // Education
        if (resumeData.getEducationList() != null && !resumeData.getEducationList().isEmpty()) {
            text.append("EDUCATION\n");
            text.append("---------\n\n");
            
            for (var edu : resumeData.getEducationList()) {
                text.append(edu.getDegree()).append(" in ").append(edu.getFieldOfStudy()).append("\n");
                text.append(edu.getInstitution()).append("\n");
                text.append(edu.getStartDate()).append(" - ").append(edu.getEndDate()).append("\n");
                
                if (edu.getGpa() > 0) {
                    text.append("GPA: ").append(edu.getGpa()).append("\n");
                }
                
                if (edu.getDescription() != null && !edu.getDescription().isEmpty()) {
                    text.append(edu.getDescription()).append("\n");
                }
                text.append("\n");
            }
        }

        return text.toString();
    }

    private String formatAsLatex(ResumeGenerationResult resumeData, ResumeTemplate template) {
        String templateContent = template.getTemplateContent();

        // Replace personal info placeholders
        templateContent = templateContent.replace("{{NAME}}", 
            sanitize(resumeData.getPersonalInfo().getFirstName() + " " + resumeData.getPersonalInfo().getLastName()));
        templateContent = templateContent.replace("{{EMAIL}}", 
            sanitize(resumeData.getPersonalInfo().getEmail() != null ? resumeData.getPersonalInfo().getEmail() : ""));
        templateContent = templateContent.replace("{{PHONE}}", 
            sanitize(resumeData.getPersonalInfo().getPhone() != null ? resumeData.getPersonalInfo().getPhone() : ""));
        templateContent = templateContent.replace("{{ADDRESS}}",
            sanitize(resumeData.getPersonalInfo().getAddress() != null ? resumeData.getPersonalInfo().getAddress() : ""));

        // Build education section
        StringBuilder educationContent = new StringBuilder();
        if (resumeData.getEducationList() != null && !resumeData.getEducationList().isEmpty()) {
            educationContent.append("  \\resumeSubHeadingListStart\n");
            for (var edu : resumeData.getEducationList()) {
                educationContent.append("    \\resumeSubheading\n")
                    .append("      {").append(sanitize(edu.getInstitution())).append("}{\n")
                    .append(dateToString(edu.getStartDate())).append(" -- ").append(edu.getEndDate() != null ? dateToString(edu.getEndDate()) : "Present").append("}\n")
                    .append("      {").append(sanitize(edu.getDegree())).append(" in ").append(sanitize(edu.getFieldOfStudy())).append("}{}");
                
                educationContent.append("  \\resumeSubHeadingListEnd\n");
                
                if (edu.getGpa() > 0 || (edu.getDescription() != null && !edu.getDescription().isEmpty())) {
                    educationContent.append("\\resumeItemListStart\n");
                    if (edu.getGpa() > 0) {
                        educationContent.append("\\resumeItem{\\textbf{GPA: ").append(sanitize(String.valueOf(edu.getGpa()))).append("}}\n");
                    }
                    if (edu.getDescription() != null && !edu.getDescription().isEmpty()) {
                        educationContent.append("\\resumeItem{").append(sanitize(edu.getDescription())).append("}\n");
                    }
                    educationContent.append("\\resumeItemListEnd");
                }
            }
        }
        templateContent = templateContent.replace("{{EDUCATION_SECTION}}", educationContent.toString());

        // Build experience section
        StringBuilder experienceContent = new StringBuilder();
        if (resumeData.getWorkExperienceList() != null && !resumeData.getWorkExperienceList().isEmpty()) {
            experienceContent.append("  \\resumeSubHeadingListStart\n");
            for (var exp : resumeData.getWorkExperienceList()) {
                experienceContent.append("    \\resumeSubheading\n")
                    .append("      {").append(sanitize(exp.getJobTitle())).append("}{")
                    .append(dateToString(exp.getStartDate())).append(" -- ").append(exp.getEndDate() != null ? dateToString(exp.getEndDate()) : "Present").append("}\n")
                    .append("      {").append(sanitize(exp.getCompany())).append("}{}\n")
                    .append("      \\resumeItemListStart\n");
                if (exp.getResponsibilities() != null && !exp.getResponsibilities().isEmpty()) {
                    experienceContent.append("        \\resumeItem{").append(sanitize(exp.getResponsibilities())).append("}\n");
                }
                if (exp.getAccomplishments() != null && !exp.getAccomplishments().isEmpty()) {
                    experienceContent.append("        \\resumeItem{").append(sanitize(exp.getAccomplishments())).append("}\n");
                }
                experienceContent.append("      \\resumeItemListEnd\n");
            }
            experienceContent.append("  \\resumeSubHeadingListEnd\n");
        }
        templateContent = templateContent.replace("{{EXPERIENCE_SECTION}}", experienceContent.toString());

        // Build skills section
        StringBuilder skillsContent = new StringBuilder();
        if (resumeData.getSkills() != null && !resumeData.getSkills().isEmpty()) {
            skillsContent.append(" \\begin{itemize}[leftmargin=0.15in, label={}]\n")
                .append("    \\small{\\item{\n")
                .append("     \\textbf{Skills}{: ").append(sanitize(String.join(", ", resumeData.getSkills()))).append("}\n")
                .append("    }}\n")
                .append(" \\end{itemize}\n");
        }
        templateContent = templateContent.replace("{{SKILLS_SECTION}}", skillsContent.toString());

        return templateContent;
    }

    private String sanitize(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\textbackslash{}")
                  .replace("%", "\\%")
                  .replace("&", "\\&")
                  .replace("#", "\\#")
                  .replace("$", "\\$")
                  .replace("_", "\\_")
                  .replace("{", "\\{")
                  .replace("}", "\\}")
                  .replace("~", "\\textasciitilde{}")
                  .replace("^", "\\textasciicircum{}");
    }

    private String dateToString(String date) {
        LocalDate obj = LocalDate.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String formatted = obj.format(formatter);
        return formatted;
    }
} 