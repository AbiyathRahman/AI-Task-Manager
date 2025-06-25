package com.insightpulse.InsightPulse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "task")
@Getter
@Setter
@SuppressWarnings("unused")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userId;
    @Column(nullable = false, name = "task_name")
    private String taskName;
    private String taskDescription;
    @Column(nullable = false, name = "task_status")
    @Enumerated(EnumType.STRING)
    private Status taskStatus;
    @Column(nullable = false, name = "task_priority")
    @Enumerated(EnumType.STRING)
    private Priority taskPriority;
    @Column(nullable = false, name = "task_due_date")
    private String taskDueDate;

    public Task() {}

    public Task(String taskName, String taskDescription, Status taskStatus, Priority taskPriority, String taskDueDate) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.taskPriority = taskPriority;
        this.taskDueDate = taskDueDate;
    }

}


