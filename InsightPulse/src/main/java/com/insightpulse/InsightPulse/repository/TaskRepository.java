package com.insightpulse.InsightPulse.repository;

import com.insightpulse.InsightPulse.model.Task;
import com.insightpulse.InsightPulse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Custom method to find by title
    Optional<Task> findByTaskName(String taskName);
    
    // Custom method to check if task exists by title
    boolean existsByTaskName(String taskName);
    
    // Custom method to delete by title
    void deleteByTaskName(String taskName);

    List<Task> findByUser(User user);
}