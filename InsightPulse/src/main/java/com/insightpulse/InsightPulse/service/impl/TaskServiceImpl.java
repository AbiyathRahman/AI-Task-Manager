package com.insightpulse.InsightPulse.service.impl;

import com.insightpulse.InsightPulse.dto.TaskRequest;
import com.insightpulse.InsightPulse.model.Task;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.repository.TaskRepository;
import com.insightpulse.InsightPulse.service.TaskService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Task createTask(TaskRequest taskRequest, User user) {
        if (taskRequest.getTaskName() == null) {
            throw new IllegalArgumentException("Task name cannot be empty");
        }

        if (taskRepository.existsByTaskName(taskRequest.getTaskName())) {
            throw new IllegalArgumentException("Task name already exists");
        }
        Task task = new Task();
        task.setTaskName(taskRequest.getTaskName());
        task.setTaskDescription(taskRequest.getTaskDescription());
        task.setTaskPriority(taskRequest.getTaskPriority());
        task.setTaskStatus(taskRequest.getTaskStatus());
        task.setUser(user);
        task.setTaskDueDate(taskRequest.getTaskDueDate());

        return taskRepository.save(task);

    }
    @Override
    public List<Task> getTaskByUser(User user){
        return taskRepository.findByUser(user);
    }

//    @Override
//    public Task getTaskById(Long id){
//        Optional<Task> task = taskRepository.findById(id);
//        return task.orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
//    }
//    @Override
//    public void deleteTaskById(Long id){
//        if(!taskRepository.existsById(id)){
//            throw new RuntimeException("Task not found with id: " + id);
//        }
//        taskRepository.deleteById(id);
//    }
//    @Override
//    public void deleteTaskByTitle(String title){
//        if(!taskRepository.existsByTaskName(title)){
//            throw new RuntimeException("Task not found with title: " + title);
//        }
//        taskRepository.deleteByTaskName(title);
//    }
//    @Override
//    public boolean existsById(Long id){
//        return taskRepository.existsById(id);
//    }
//    @Override
//    public boolean existsByTitle(String title){
//        return taskRepository.existsByTaskName(title);
//    }
}

