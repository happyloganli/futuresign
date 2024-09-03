package com.futuresign.syntax_service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SyntaxService {

    private final VertexClient vertexClient;
    private final String syntaxPrompt;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${signing.exchange.name}")
    private String SIGNING_EXCHANGE_NAME;
    @Value("${syntax.check.routing.key}")
    private String SYNTAX_CHECK_ROUTING_KEY;
    @Value("${syntax.check.queue}")
    private String SYNTAX_CHECK_QUEUE;

    public SyntaxService(VertexClient vertexClient, @Value("${syntax.check.prompt}") String syntaxPrompt) {
        this.vertexClient = vertexClient;
        this.syntaxPrompt = syntaxPrompt;
    }

    public String getSyntaxSuggestion(String input) throws IOException {
        String prompt = syntaxPrompt + input;
        return vertexClient.textInput(prompt);
    }

    private String checkSyntax(String fileKey, String username) {
        return "Checked syntax" + fileKey + username;
    }

    @RabbitListener(queues = "syntax_check_queue")
    public void consumeSyntaxCheckEvent(SyntaxCheckEvent event) {
        try {
            System.out.println("Received syntax check event: " + event);
            String fileKey = event.getFileKey();
            String username = event.getUsername();

            String checkResult = checkSyntax(fileKey, username);

            publishSyntaxCheckFinishedEvent(fileKey, username, checkResult);
        } catch (Exception e) {
            // Log the exception and handle it
            System.err.println("Error processing syntax check event: " + e.getMessage());
            e.printStackTrace();
            // Optionally, handle the exception or rethrow it depending on your logic
        }
    }

    private void publishSyntaxCheckFinishedEvent(String fileKey, String username, String checkResult) {
        SyntaxCheckFinishedEvent event = new SyntaxCheckFinishedEvent(fileKey, username, checkResult);
        rabbitTemplate.convertAndSend(SIGNING_EXCHANGE_NAME, SYNTAX_CHECK_ROUTING_KEY, event);
    }
}
