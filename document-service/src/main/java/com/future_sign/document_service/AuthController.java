package com.future_sign.document_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;

    public AuthController(JwtUtil jwtUtil, UserServiceClient userServiceClient) {
        this.jwtUtil = jwtUtil;
        this.userServiceClient = userServiceClient;
    }

    @PostMapping("/token")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        logger.info("Received authentication request for user: {}", authRequest.getUsername());
        if (userServiceClient.authenticateUser(authRequest.getUsername(), authRequest.getPassword())) {
            String jwt = jwtUtil.generateToken(authRequest.getUsername());
            logger.info("JWT token generated for user: {}", authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt));
        } else {
            logger.warn("Authentication failed for user: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        if (jwtUtil.canTokenBeRefreshed(token.substring(7))) {
            String newToken = jwtUtil.generateToken(username);
            return ResponseEntity.ok(new AuthResponse(newToken));
        }
        return ResponseEntity.badRequest().body("Token can't be refreshed");
    }
}

class AuthRequest {
    private String username;
    private String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

record AuthResponse(String jwt) {}