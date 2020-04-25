package com.edson.gae_final.exception;

public class UserNotFoundException extends Exception {

    private String message;

    public UserNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}