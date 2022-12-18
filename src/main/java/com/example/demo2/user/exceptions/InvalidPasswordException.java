package com.example.demo2.user.exceptions;

public class InvalidPasswordException extends Exception {
    public InvalidPasswordException() {
        super("Invalid Password");
    }
}
