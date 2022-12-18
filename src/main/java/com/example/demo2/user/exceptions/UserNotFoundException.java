package com.example.demo2.user.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String email) {
        super("User with email="+email+" does not exist");
    }

    public UserNotFoundException(Long userId) {
        super("User with userId="+userId+" does not exist");
    }
}
