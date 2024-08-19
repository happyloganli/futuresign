package com.future_sign.user_service;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String userNotFound) {
        super(userNotFound);
    }
}
