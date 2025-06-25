package com.insightpulse.InsightPulse.service;


import com.insightpulse.InsightPulse.dto.TaskRequest;
import com.insightpulse.InsightPulse.model.Task;
import com.insightpulse.InsightPulse.model.User;

public interface TaskService {

    Task createTask(TaskRequest task, User user);
//    Task getTaskById(Long id);
//    void deleteTaskById(Long id);
//    void deleteTaskByTitle(String title);
//    boolean existsById(Long id);
//    boolean existsByTitle(String title);

}
