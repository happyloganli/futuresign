package com.futuresign.syntax_service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VertexClient {

    @Value("${google.project.id}")
    private String projectId;
    @Value("${google.location}")
    private String location;
    @Value("${google.module.name}")
    private String modelName;

    public String textInput(String prompt) throws IOException {

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            GenerateContentResponse response = model.generateContent(prompt);
            String output = ResponseHandler.getText(response);
            return output;
        }
    }

}
