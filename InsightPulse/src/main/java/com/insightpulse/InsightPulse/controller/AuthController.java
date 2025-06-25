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
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(), authRequest.getPassword()
                )
        );
        String token = jwtUtil.generateToken(authRequest.getUsername());
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public String register(@RequestBody AuthRequest authRequest){
        // Check if user already exists
        if (userService.existsByUsername(authRequest.getUsername())) {
            return "Username already exists";
        }
        
        User user = new User();
        user.setName(authRequest.getUsername()); // You might want to add a separate name field
        user.setUsername(authRequest.getUsername());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        userService.createUser(user);
        return "User registered successfully";
    }
}