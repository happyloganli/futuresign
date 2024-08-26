package com.future_sign.user_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserById(Long id) throws ResourceNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public ResponseEntity<String> authenticateUser(String username, String password) {
        // Fetch user by username
        Optional<User> userOptional = userRepository.findByUsername(username);

        // Check if user exists and password matches
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // If authentication is successful, return the UserDto
                return ResponseEntity.ok().body("Authentication successful");
            }
        }

        // If authentication fails, return null
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
    }
}