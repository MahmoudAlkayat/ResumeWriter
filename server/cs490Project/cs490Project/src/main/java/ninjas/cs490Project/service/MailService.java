package ninjas.cs490Project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("curlyheadbelal@gmail.com"); // Set a valid sender address
        message.setTo(toEmail);
        message.setSubject("Password Reset");
        message.setText("Click the following link to reset your password: " + resetLink);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String verifyLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("curlyheadbelal@gmail.com"); // Set a valid sender address
        message.setTo(toEmail);
        message.setSubject("Verify Your Account");
        message.setText("Click the following link to verify your account: " + verifyLink);
        mailSender.send(message);
    }
}
