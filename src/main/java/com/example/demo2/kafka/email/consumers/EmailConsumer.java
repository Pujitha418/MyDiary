package com.example.demo2.kafka.email.consumers;

import com.example.demo2.kafka.KafkaConstants;
import com.example.demo2.kafka.email.models.SendEmailApiParams;
import com.example.demo2.reminders.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {
    private final EmailService emailService;

    @Autowired
    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = KafkaConstants.EMAIL_TOPIC,
            groupId = KafkaConstants.EMAIL_GROUP_ID
    )
    public void listen(SendEmailApiParams emailApiParams) {
        System.out.println("sending via kafka listener..");
        System.out.println("emailApiParams.getEmail() = " + emailApiParams.getToEmail());
        emailService.sendEmail(emailApiParams);
    }
}
