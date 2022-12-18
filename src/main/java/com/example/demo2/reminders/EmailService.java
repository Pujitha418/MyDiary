package com.example.demo2.reminders;

import com.example.demo2.kafka.email.models.SendEmailApiParams;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private JavaMailSender emailSender;
    private Logger logger;

    @Autowired
    public EmailService(JavaMailSender emailSender, Logger logger) {
        this.emailSender = emailSender;
        this.logger = logger;
    }

    public void sendEmail(SendEmailApiParams sendEmailApiParams) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(sendEmailApiParams.getSubject());
        mailMessage.setFrom("mydiaryreminders@gmail.com");
        mailMessage.setTo(sendEmailApiParams.getToEmail());
        mailMessage.setText("Hello "+sendEmailApiParams.getName());

        emailSender.send(mailMessage);
        logger.info("sent mail");
    }
}
