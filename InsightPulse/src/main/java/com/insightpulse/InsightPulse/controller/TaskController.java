package com.insightpulse.InsightPulse.controller;

import com.insightpulse.InsightPulse.dto.TaskRequest;
import com.insightpulse.InsightPulse.dto.TaskResponse;
import com.insightpulse.InsightPulse.model.Task;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.repository.UserRepository;
import com.insightpulse.InsightPulse.security.JwtUtil;
import com.insightpulse.InsightPulse.service.TaskService;
import com.insightpulse.InsightPulse.service.impl.BedrockAIService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BedrockAIService bedrockAIService;

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
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskRequest taskRequest){
        if(taskRequest.getTaskName() == null){
            throw new IllegalArgumentException("Task name cannot be empty");
        }
        var taskToUpdate = taskService.getTaskById(id);
        if(taskToUpdate != null){
            taskService.updateTask(taskRequest, taskToUpdate);
            return ResponseEntity.ok(taskToUpdate);

        }else{
            throw new RuntimeException("Task not found with id: " + id);
        }


    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@PathVariable Long id){
        taskService.deleteTaskById(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/insights")
    public ResponseEntity<String> getAIInsights(HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<Task> tasks = taskService.getTaskByUser(user);
        String aiFeedback = bedrockAIService.getTaskInsight(tasks);
        return ResponseEntity.ok(aiFeedback);
    }


}