package com.futuresign.syntax_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SyntaxSuggestionService {

    private final VertexClient vertexClient;
    private final String syntaxPrompt;

    public SyntaxSuggestionService(VertexClient vertexClient, @Value("${syntax.check.prompt}") String syntaxPrompt) {
        this.vertexClient = vertexClient;
        this.syntaxPrompt = syntaxPrompt;
    }

    public String getSyntaxSuggestion(String input) throws IOException {
        String prompt = syntaxPrompt + input;
        return vertexClient.textInput(prompt);
    }
}
