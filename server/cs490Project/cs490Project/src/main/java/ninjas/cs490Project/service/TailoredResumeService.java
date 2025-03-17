package ninjas.cs490Project.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import ninjas.cs490Project.dto.ResumeData;
import ninjas.cs490Project.entity.JobListing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class TailoredResumeService {

    private static final Logger logger = LoggerFactory.getLogger(TailoredResumeService.class);

    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;

    /**
     * Generates HTML using Thymeleaf template "tailoredResume.html".
     */
    public String generateHtml(ResumeData resumeData, JobListing jobListing, List<String> matchingSkills) {
        logger.info("Generating HTML for tailored resume.");
        Context context = new Context();
        context.setVariable("resumeData", resumeData);
        context.setVariable("jobListing", jobListing);
        context.setVariable("matchingSkills", matchingSkills);

        // Loads the "tailoredResume.html" file from src/main/resources/templates
        String html = thymeleafTemplateEngine.process("tailoredResume", context);
        logger.info("Successfully generated HTML for tailored resume.");
        return html;
    }

    /**
     * Generates a PDF from the HTML content using OpenHTMLToPDF.
     * @param resumeData Resume data object
     * @param jobListing Job listing entity
     * @param matchingSkills List of matching skills
     * @return A byte array containing the generated PDF
     * @throws Exception if PDF generation fails
     */
    public byte[] generateTailoredPdf(ResumeData resumeData, JobListing jobListing, List<String> matchingSkills) throws Exception {
        logger.info("Starting PDF generation for tailored resume.");
        String html = generateHtml(resumeData, jobListing, matchingSkills);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            logger.info("Successfully generated PDF for tailored resume.");
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating tailored PDF: {}", e.getMessage(), e);
            throw e;
        }
    }
}
