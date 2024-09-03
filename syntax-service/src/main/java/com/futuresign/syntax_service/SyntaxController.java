package com.futuresign.syntax_service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SyntaxController {

    private final SyntaxService syntaxService;

    public SyntaxController(SyntaxService syntaxService) {
        this.syntaxService = syntaxService;
    }

    @PostMapping("/suggest")
    public String getSuggestion(@RequestBody String input) {
        try {
            return syntaxService.getSyntaxSuggestion(input);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
