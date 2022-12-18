package com.example.demo2.user.exceptions;

public class EmailAlreadyExistsException extends Exception {
    public EmailAlreadyExistsException(String email) {
        super("Email "+email+" already registered");
    }
}
