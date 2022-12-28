package com.example.demo2.kafka.email.consumers;

import com.example.demo2.kafka.KafkaConstants;
import com.example.demo2.kafka.email.models.SendEmailApiParams;
import com.example.demo2.kafka.email.services.EmailPostCallableExecutionFactory;
import com.example.demo2.reminders.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service
public class EmailConsumer {
    private final EmailService emailService;
    private final EmailPostCallableExecutionFactory postCallableExecutionFactory;

    @Autowired
    public EmailConsumer(EmailService emailService, EmailPostCallableExecutionFactory postCallableExecutionFactory) {
        this.emailService = emailService;
        this.postCallableExecutionFactory = postCallableExecutionFactory;
    }

    @KafkaListener(
            topics = KafkaConstants.EMAIL_TOPIC,
            groupId = KafkaConstants.EMAIL_GROUP_ID
    )
    public void listen(SendEmailApiParams emailApiParams) {
        System.out.println("sending via kafka listener..");
        System.out.println("emailApiParams.getEmail() = " + emailApiParams.getToEmail());
        emailService.sendEmail(emailApiParams);
        if (! emailApiParams.getPostCallable().isEmpty()) {
            Iterator<Map.Entry<String, Long>> postCallablesIter = emailApiParams.getPostCallable().entrySet().iterator();
            while ( (postCallablesIter.hasNext())) {
                Map.Entry<String, Long> entry = postCallablesIter.next();
                postCallableExecutionFactory.execute(entry.getKey(), entry.getValue(), "SENT");
            }
        }
    }
}
