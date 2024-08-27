package com.futuresign.syntax_service;

public class VertexRequest {
    private String projectId;
    private String location;
    private String modelName;
    private String textPrompt;

    public VertexRequest(String projectId, String location, String modelName, String textPrompt) {
        this.projectId = projectId;
        this.location = location;
        this.modelName = modelName;
        this.textPrompt = textPrompt;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTextPrompt() {
        return textPrompt;
    }

    public void setTextPrompt(String textPrompt) {
        this.textPrompt = textPrompt;
    }
}
