package com.example.demo2.common.exceptions;

public class Unauthorized extends Exception {
    public Unauthorized() {
        super("Unauthorized request");
    }
}
