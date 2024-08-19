package com.future_sign.document_service;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String documentNotFound) {
        super(documentNotFound);
    }
}
