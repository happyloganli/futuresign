package com.futuresign.syntax_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SyntaxSuggestionController {

    private final SyntaxSuggestionService syntaxSuggestionService;

    public SyntaxSuggestionController(SyntaxSuggestionService syntaxSuggestionService) {
        this.syntaxSuggestionService = syntaxSuggestionService;
    }

    @PostMapping("/suggest")
    public String getSuggestion(@RequestBody String input) {
        try {
            return syntaxSuggestionService.getSyntaxSuggestion(input);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
