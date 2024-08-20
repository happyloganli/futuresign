package com.future_sign.document_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/api/users/authenticate")
    ResponseEntity<UserDto> authenticateUser(@RequestBody AuthenticationRequest request);
}
