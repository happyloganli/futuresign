package com.future_sign.document_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${user.service.apikey}")
    private String apiKey;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean authenticateUser(String username, String password) {
        AuthRequest authRequest = new AuthRequest(username, password);
        String url = userServiceUrl + "/api/users/authenticate";
        logger.info("Sending authentication request to: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);  // Adjust the header name if necessary
        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, headers);

        try {
            ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Void.class);
            logger.info("Received response status: {}", response.getStatusCode());
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            logger.error("Authentication failed. Status code: {}, Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication", e);
            return false;
        }
    }
}

