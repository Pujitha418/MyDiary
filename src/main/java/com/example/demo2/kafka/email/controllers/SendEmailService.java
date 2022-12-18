package com.example.demo2.kafka.email.controllers;

import com.example.demo2.kafka.KafkaConstants;
import com.example.demo2.kafka.email.models.SendEmailApiParams;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class SendEmailService {
    private KafkaTemplate<String, SendEmailApiParams> kafkaTemplate;
    private final String EMAIL_TOPIC = KafkaConstants.EMAIL_TOPIC;
    private Logger logger;


    @Autowired
    public SendEmailService(KafkaTemplate<String, SendEmailApiParams> kafkaTemplate, Logger logger) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger;
    }

    //@PostMapping(path = "/")
    public String sendEmailEventToKafka(SendEmailApiParams sendEmailApiParams) {
        logger.info("Insider sendEmailEventToKafka");
        ListenableFuture<SendResult<String, SendEmailApiParams>> future = kafkaTemplate.send(EMAIL_TOPIC, sendEmailApiParams);
        logger.info("future-", future);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to publish email event for email=["
                        + sendEmailApiParams.getToEmail() + "] due to : " + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, SendEmailApiParams> result) {
                System.out.println("Sent email event for email=[" + sendEmailApiParams.getToEmail() +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
        });
        System.out.println("testing async");
        return "PUBLISHED";
    }
}
