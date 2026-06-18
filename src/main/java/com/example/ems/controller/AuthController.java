package com.example.ems.controller;

import com.example.ems.dto.LoginRequest;
import com.example.ems.dto.RegisterRequest;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.AuthService;
import com.example.ems.config.JwtUtil; // Added Import
import com.example.ems.config.CustomUserDetailsService; // Added Import
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // HIGH INDUSTRY FIX: JwtUtil aur custom user details service inject kiye
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // Updated Constructor Injection
    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
                          UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // 1. SIGNUP API
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String response = authService.registerUser(request);
            return ResponseEntity.ok(Map.of("message", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. LOGIN API (HIGH INDUSTRY JWT REWRITE)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Database se user check karein
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email not found in database!"));

            // Password verification check using BCrypt
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid password!"));
            }

            // Spring Security Manager ke throw official authentication trigger karna
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // UserDetails fetch karke stateless JWT Token generate karna
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            String role = user.getRole().getName();
            System.out.println("--- [SUCCESS] JWT Token Generated for: " + user.getEmail() + " | Role: " + role + " ---");

            // Response me Token ko pass karna (Standard Industry Structure)
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful!",
                    "token", token,  // Ab frontend is token ko save karega
                    "role", role,
                    "email", user.getEmail()
            ));
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }
}