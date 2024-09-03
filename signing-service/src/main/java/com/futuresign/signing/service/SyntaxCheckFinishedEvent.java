package com.futuresign.signing.service;

public class SyntaxCheckFinishedEvent {
    private String fileKey;
    private String username;
    private String result;

    public SyntaxCheckFinishedEvent(String fileKey, String username, String result) {
        this.fileKey = fileKey;
        this.username = username;
        this.result = result;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
