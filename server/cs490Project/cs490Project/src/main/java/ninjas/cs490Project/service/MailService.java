package ninjas.cs490Project.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String toEmail, String resetLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset");

            // Build nicely formatted HTML content
            String htmlContent = "<html>" +
                    "  <body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>" +
                    "    <table role='presentation' style='width: 100%; border-collapse: collapse;'>" +
                    "      <tr>" +
                    "        <td align='center' style='padding: 20px; background-color: #f2f2f2;'>" +
                    "          <table role='presentation' style='width: 600px; background-color: #ffffff; padding: 20px; border-radius: 4px;'>" +
                    "            <tr>" +
                    "              <td style='text-align: center;'>" +
                    "                <h2 style='color: #333;'>Password Reset</h2>" +
                    "                <p style='font-size: 16px; color: #666;'>Click the button below to reset your password:</p>" +
                    "                <a href='" + resetLink + "' style='display: inline-block; margin-top: 20px; padding: 10px 20px; " +
                    "                  background-color: #007BFF; color: #ffffff; text-decoration: none; border-radius: 4px;'>Reset Password</a>" +
                    "                <p style='font-size: 14px; color: #999; margin-top: 20px;'>If you did not request a password reset, please ignore this email.</p>" +
                    "              </td>" +
                    "            </tr>" +
                    "          </table>" +
                    "        </td>" +
                    "      </tr>" +
                    "    </table>" +
                    "  </body>" +
                    "</html>";
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Log the error; you might want to use a logging framework like SLF4J in a real application.
            System.err.println("Error sending reset link email: " + e.getMessage());
        }
    }
    public void sendVerificationEmail(String toEmail, String verifyLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            // Set a valid "from" address (ensure your SMTP settings permit this)
            helper.setFrom("noreply@yourdomain.com");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Account");

            // Build a nicely formatted HTML content
            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                    "  <div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                    "    <h2 style='color: #333;'>Email Verification</h2>" +
                    "    <p style='font-size: 16px; color: #555;'>Thank you for registering. Please click the button below to verify your email address and activate your account.</p>" +
                    "    <div style='text-align: center; margin: 30px 0;'>" +
                    "      <a href='" + verifyLink + "' style='display: inline-block; padding: 12px 25px; background-color: #28a745; color: #ffffff; text-decoration: none; border-radius: 4px; font-size: 16px;'>Verify Email</a>" +
                    "    </div>" +
                    "    <p style='font-size: 14px; color: #777;'>If you did not create an account, please ignore this email.</p>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Error sending verification email: " + e.getMessage());
        }
    }

}
