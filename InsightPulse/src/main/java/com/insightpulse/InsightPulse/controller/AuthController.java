package com.insightpulse.InsightPulse.controller;

import com.insightpulse.InsightPulse.dto.AuthRequest;
import com.insightpulse.InsightPulse.dto.AuthResponse;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.security.JwtUtil;
import com.insightpulse.InsightPulse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("unused")
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @PostMapping("/auth/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest){
        System.out.println("=== Auth Controller Debug ===");
        System.out.println("Login endpoint hit!");
        System.out.println("Username: " + authRequest.getUsername());
        System.out.println("Password received: " + (authRequest.getPassword() != null ? "[PRESENT]" : "[NULL]"));

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(), authRequest.getPassword()
                    )
            );
            System.out.println("Authentication successful for: " + authRequest.getUsername());

            String token = jwtUtil.generateToken(authRequest.getUsername());
            System.out.println("JWT token generated successfully");

            return new AuthResponse(token);
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/auth/register")
    public String register(@RequestBody AuthRequest authRequest){
        System.out.println("=== Auth Controller Debug ===");
        System.out.println("Register endpoint hit!");
        System.out.println("Username: " + authRequest.getUsername());
        System.out.println("Name: " + authRequest.getName());

        // Check if user already exists
        if (userService.existsByUsername(authRequest.getUsername())) {
            System.out.println("User already exists: " + authRequest.getUsername());
            return "Username already exists";
        }

        User user = new User();
        user.setName(authRequest.getName());
        user.setUsername(authRequest.getUsername());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        userService.createUser(user);
        System.out.println("User registered successfully: " + authRequest.getUsername());
        return "User registered successfully";
    }
}