package com.insightpulse.InsightPulse.service;

import com.insightpulse.InsightPulse.model.Task;

public interface TaskService {

    Task createTask(Task task);
    Task getTaskById(Long id);
    void deleteTaskById(Long id);
    void deleteTaskByTitle(String title);
    boolean existsById(Long id);
    boolean existsByTitle(String title);
}
