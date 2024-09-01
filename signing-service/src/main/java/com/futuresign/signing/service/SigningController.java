package com.futuresign.signing.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> checkSignatureStatus(@RequestParam String id) {
        try {
            StatusResponse response = signingService.checkSignatureStatus(id);
            return ResponseEntity.ok().body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking signature status: " + e.getMessage());
        }
    }
}

class SignatureRequest {
    private String username;
    private String filekey;

    public SignatureRequest(String username, String filekey) {
        this.username = username;
        this.filekey = filekey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilekey() {
        return filekey;
    }

    public void setFilekey(String filekey) {
        this.filekey = filekey;
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