package com.insightpulse.InsightPulse.controller;

import com.insightpulse.InsightPulse.dto.TaskRequest;
import com.insightpulse.InsightPulse.dto.TaskResponse;
import com.insightpulse.InsightPulse.model.Task;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.repository.UserRepository;
import com.insightpulse.InsightPulse.security.JwtUtil;
import com.insightpulse.InsightPulse.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TaskService taskService;

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

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest taskRequest, HttpServletRequest request ){
        User user = getCurrentUser(request);
        Task task = taskService.createTask(taskRequest, user);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<Iterable<Task>> getTaskByUser(HttpServletRequest request){
        User user = getCurrentUser(request);
        Iterable<Task> tasks = taskService.getTaskByUser(user);
        return ResponseEntity.ok(tasks);
    }

}