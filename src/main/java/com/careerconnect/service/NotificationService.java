package com.careerconnect.service;

import com.careerconnect.enums.ApplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.from:no-reply@careerconnect.local}")
    private String from;

    @Autowired
    public NotificationService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendStatusUpdate(String to, String name, String jobTitle, String company, ApplicationStatus status) {
        String subject = "Your application for " + jobTitle + " is now " + status.name().replace('_', ' ');
        String body = "Hi " + name + ",\n\nThe status of your application for \"" + jobTitle + "\" at " + company
                + " has been updated to: " + status.name().replace('_', ' ')
                + ".\n\nLog in to CareerConnect to see the full timeline.\n\n- Team CareerConnect";
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[EMAIL not configured] To: {} | Subject: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        } catch (Exception e) {
            log.warn("Could not send email to {}: {}", to, e.getMessage());
        }
    }
}