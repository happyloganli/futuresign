package com.future_sign.document_service;

public class UserDto {
    private String username;
    private String email;
    // Add any other fields you want to transfer, but NOT the password

    // Constructors
    public UserDto() {}

    public UserDto(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
