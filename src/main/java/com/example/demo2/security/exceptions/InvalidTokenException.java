package com.example.demo2.security.exceptions;

public class InvalidTokenException extends Exception {
    public InvalidTokenException() {
        super("Invalid Token");
    }
}
