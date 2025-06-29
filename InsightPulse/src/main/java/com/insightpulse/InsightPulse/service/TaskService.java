package com.insightpulse.InsightPulse.service;


import com.insightpulse.InsightPulse.dto.TaskRequest;
import com.insightpulse.InsightPulse.model.Task;
import com.insightpulse.InsightPulse.model.User;

import java.util.List;

public interface TaskService {

    Task createTask(TaskRequest task, User user);
    List<Task> getTaskByUser(User user);
    Task updateTask(TaskRequest taskRequest, Task task);
    Task getTaskById(Long id);
//    void deleteTaskById(Long id);
//    void deleteTaskByTitle(String title);
//    boolean existsById(Long id);
//    boolean existsByTitle(String title);

}
