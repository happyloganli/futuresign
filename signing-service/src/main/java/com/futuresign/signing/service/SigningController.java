package com.futuresign.signing.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signature")
public class SigningController {
    @Autowired
    private SigningService signingService;

    @PostMapping("/request")
    public ResponseEntity<?> requestSignature(@RequestBody SignatureRequest request) {
        try {
            SignatureResponse response = signingService.requestSignature(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing signature request: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkSignatureStatus(@RequestBody StatusRequest request) {
        try {
            StatusResponse response = signingService.checkSignatureStatus(request.getId());
            return ResponseEntity.ok().body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking signature status: " + e.getMessage());
        }
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}

class SignatureRequest {
    private String username;
    private String fileKey;

    public SignatureRequest(String username, String fileKey) {
        this.username = username;
        this.fileKey = fileKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}

class SignatureResponse {
    private String message;
    private String id;

    public SignatureResponse(String message, String id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

class StatusRequest{
    private String id;

    public StatusRequest() {
    }

    public StatusRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

class StatusResponse {
    private String status;

    public StatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}