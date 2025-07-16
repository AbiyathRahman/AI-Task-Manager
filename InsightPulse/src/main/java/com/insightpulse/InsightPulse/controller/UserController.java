package com.insightpulse.InsightPulse.controller;

import com.insightpulse.InsightPulse.dto.AuthRequest;
import com.insightpulse.InsightPulse.dto.UserRequest;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.repository.UserRepository;
import com.insightpulse.InsightPulse.security.JwtUtil;
import com.insightpulse.InsightPulse.service.UserService;
import com.insightpulse.InsightPulse.service.impl.MyUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @GetMapping("/you")
    public User getUser(HttpServletRequest request){
        User user = getCurrentUser(request);
        if(user != null){
            return user;
        }else{
            throw new RuntimeException("User not found with username: " + user.getUsername());
        }
    }

    @PutMapping("/change-name")
    public void changename(@RequestBody UserRequest userRequest , HttpServletRequest request ){
        User user = getCurrentUser(request);
        if(userRequest.getName() == null){
            throw new IllegalArgumentException("Name cannot be empty");
        }
        user.setName(userRequest.getName());
        userService.updateUserName(user);

    }
    @PutMapping("/change-tier")
    public void basicTier(@RequestBody UserRequest userRequest, HttpServletRequest request){
        User user = getCurrentUser(request);
        if(userRequest.getTier() == null){
            throw new IllegalArgumentException("Tier cannot be empty");
        }
        if(userRequest.getTier() == user.getTier()){
            throw new IllegalArgumentException("Tier cannot be the same as the current tier");
        }
        user.setTier(userRequest.getTier());
        userService.updateUserTier(user);
    }
}
