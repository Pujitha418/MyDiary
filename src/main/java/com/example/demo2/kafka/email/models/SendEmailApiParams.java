package com.example.demo2.kafka.email.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailApiParams {
    private String name;
    private String toEmail;
    private String subject;
    private Map<String, Long> postCallable;
}