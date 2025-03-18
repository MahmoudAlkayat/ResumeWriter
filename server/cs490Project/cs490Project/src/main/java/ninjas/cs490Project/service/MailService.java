package ninjas.cs490Project.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource; // <-- Important for loading from resources
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a password reset email with an inline image (logo).
     */
    public void sendResetLink(String toEmail, String resetLink) {
        try {
            // 'true' in the constructor to allow multipart (for inline images)
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");

            // Build your HTML. Use 'cid:logoImage' for the inline image reference.
            String htmlContent = "<html>" +
                    "  <head>" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                    "  </head>" +
                    "  <body style=\"font-family: 'Segoe UI', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">" +
                    "    <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse; background-color: #f9f9f9;\">" +
                    "      <tr>" +
                    "        <td align=\"center\" style=\"padding: 40px 0;\">" +
                    "          <table role=\"presentation\" style=\"width: 600px; max-width: 90%; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\">" +
                    "            <tr>" +
                    "              <td style=\"padding: 0;\">" +
                    "                <div style=\"height: 6px; background: linear-gradient(to right, #4776E6, #8E54E9); border-radius: 8px 8px 0 0;\"></div>" +
                    "              </td>" +
                    "            </tr>" +
                    "            <tr>" +
                    "              <td style=\"padding: 30px 40px; text-align: center;\">" +
                    // Use the content ID here:
                    "<img src=\"cid:logoImage\" alt=\"Logo\" style=\"max-width: 150px; max-height: 150px; margin-bottom: 20px;\">" +
                    "                <h1 style=\"color: #333333; font-size: 24px; font-weight: 600; margin: 0 0 15px 0;\">Reset Your Password</h1>" +
                    "                <p style=\"color: #666666; font-size: 16px; line-height: 24px; margin: 0 0 25px 0;\">We received a request to reset your password. Click the button below to create a new password:</p>" +
                    "                <div style=\"margin: 30px 0;\">" +
                    "                  <a href=\"" + resetLink + "\" style=\"display: inline-block; background: linear-gradient(to right, #4776E6, #8E54E9); color: white; font-weight: 500; text-decoration: none; padding: 12px 30px; border-radius: 4px; font-size: 16px;\">Reset Password</a>" +
                    "                </div>" +
                    "                <p style=\"color: #666666; font-size: 14px; line-height: 22px; margin: 0 0 15px 0;\">This link will expire in 30 minutes. If you didn't request a password reset, you can safely ignore this email.</p>" +
                    "                <hr style=\"border: 0; border-top: 1px solid #eeeeee; margin: 30px 0;\">" +
                    "                <p style=\"color: #999999; font-size: 13px; line-height: 20px; margin: 0;\">If the button above doesn't work, copy and paste this link into your browser:</p>" +
                    "                <p style=\"color: #666666; font-size: 13px; line-height: 20px; margin: 5px 0 0 0; word-break: break-all;\"><a href=\"" + resetLink + "\" style=\"color: #5f7df2; text-decoration: none;\">" + resetLink + "</a></p>" +
                    "              </td>" +
                    "            </tr>" +
                    "            <tr>" +
                    "              <td style=\"background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 0 0 8px 8px;\">" +
                    "                <p style=\"color: #999999; font-size: 13px; margin: 0;\">© 2025 ResumeElite. All rights reserved.</p>" +
                    "                <p style=\"color: #999999; font-size: 12px; margin: 10px 0 0 0;\">15 Greek Way, Newark, NJ, USA</p>" +
                    "              </td>" +
                    "            </tr>" +
                    "          </table>" +
                    "        </td>" +
                    "      </tr>" +
                    "    </table>" +
                    "  </body>" +
                    "</html>";

            // Enable HTML
            helper.setText(htmlContent, true);

            // Add inline image from classpath (resources/images/eliteResume.png)
            // "logoImage" must match 'cid:logoImage' in the HTML
            ClassPathResource logo = new ClassPathResource("images/EliteResume.png");
            helper.addInline("logoImage", logo);

            // Finally, send
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Error sending reset link email: " + e.getMessage());
        }
    }

    /**
     * Sends a verification email with an inline image (logo).
     */
    public void sendVerificationEmail(String toEmail, String verifyLink) {
        try {
            // 'true' for multipart
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("noreply@yourdomain.com");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Account");

            String htmlContent = "<html>" +
                    "  <head>" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                    "  </head>" +
                    "  <body style=\"font-family: 'Segoe UI', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">" +
                    "    <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse; background-color: #f9f9f9;\">" +
                    "      <tr>" +
                    "        <td align=\"center\" style=\"padding: 40px 0;\">" +
                    "          <table role=\"presentation\" style=\"width: 600px; max-width: 90%; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\">" +
                    "            <tr>" +
                    "              <td style=\"padding: 0;\">" +
                    "                <div style=\"height: 6px; background: linear-gradient(to right, #28a745, #20c997); border-radius: 8px 8px 0 0;\"></div>" +
                    "              </td>" +
                    "            </tr>" +
                    "            <tr>" +
                    "              <td style=\"padding: 30px 40px; text-align: center;\">" +
                    "                <img src=\"cid:logoImage\" alt=\"Logo\" style=\"width: 80px; height: 80px; margin-bottom: 20px;\">" +
                    "                <h1 style=\"color: #333333; font-size: 24px; font-weight: 600; margin: 0 0 15px 0;\">Verify Your Email Address</h1>" +
                    "                <p style=\"color: #666666; font-size: 16px; line-height: 24px; margin: 0 0 25px 0;\">Thank you for registering! To complete your account setup and access all features, please verify your email address:</p>" +
                    "                <div style=\"margin: 30px 0;\">" +
                    "                  <a href=\"" + verifyLink + "\" style=\"display: inline-block; background: linear-gradient(to right, #28a745, #20c997); color: white; font-weight: 500; text-decoration: none; padding: 12px 30px; border-radius: 4px; font-size: 16px;\">Verify Email</a>" +
                    "                </div>" +
                    "                <p style=\"color: #666666; font-size: 14px; line-height: 22px; margin: 0 0 15px 0;\">This verification link will expire in 24 hours. If you did not create an account with us, you can safely ignore this email.</p>" +
                    "                <hr style=\"border: 0; border-top: 1px solid #eeeeee; margin: 30px 0;\">" +
                    "                <p style=\"color: #999999; font-size: 13px; line-height: 20px; margin: 0;\">If the button above doesn't work, copy and paste this link into your browser:</p>" +
                    "                <p style=\"color: #666666; font-size: 13px; line-height: 20px; margin: 5px 0 0 0; word-break: break-all;\"><a href=\"" + verifyLink + "\" style=\"color: #28a745; text-decoration: none;\">" + verifyLink + "</a></p>" +
                    "              </td>" +
                    "            </tr>" +
                    "            <tr>" +
                    "              <td style=\"background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 0 0 8px 8px;\">" +
                    "                <p style=\"color: #999999; font-size: 13px; margin: 0;\">©  ResumeElite. All rights reserved.</p>" +
                    "                <p style=\"color: #999999; font-size: 12px; margin: 10px 0 0 0;\">15 Greek Way, Newark, NJ, USA</p>" +
                    "              </td>" +
                    "            </tr>" +
                    "          </table>" +
                    "        </td>" +
                    "      </tr>" +
                    "    </table>" +
                    "  </body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            // Attach the local image inline
            ClassPathResource logo = new ClassPathResource("images/EliteResume.png");
            helper.addInline("logoImage", logo);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Error sending verification email: " + e.getMessage());
        }
    }
}