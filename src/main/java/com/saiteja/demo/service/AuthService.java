package com.saiteja.demo.service;

import com.saiteja.demo.dto.RegisterRequest;
import com.saiteja.demo.dto.LoginRequest;
import com.saiteja.demo.dto.AuthResponse;
import com.saiteja.demo.exception.BadRequestException;
import com.saiteja.demo.model.User;
import com.saiteja.demo.repository.UserRepository;
import com.saiteja.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                null,
                savedUser.getUsername(),
                savedUser.getRole(),
                "User registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole(),
                "Login successful"
        );
    }
}