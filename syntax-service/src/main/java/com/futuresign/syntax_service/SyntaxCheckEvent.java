package com.futuresign.syntax_service;

import java.io.Serializable;

public class SyntaxCheckEvent{
    private String fileKey;
    private String username;

    public SyntaxCheckEvent(String fileKey, String username) {
        this.fileKey = fileKey;
        this.username = username;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
