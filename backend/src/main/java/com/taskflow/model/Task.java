package com.taskflow.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_user", columnList = "user_id"),
        @Index(name = "idx_task_user_status", columnList = "user_id, status"),
        @Index(name = "idx_task_user_created", columnList = "user_id, created_at DESC")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED;

        public static TaskStatus fromApiString(String s) {
            if (s == null) return PENDING;
            return switch (s) {
                case "pending" -> PENDING;
                case "in-progress", "in_progress", "inProgress" -> IN_PROGRESS;
                case "completed" -> COMPLETED;
                default -> PENDING;
            };
        }

        public String toApiString() {
            return switch (this) {
                case PENDING -> "pending";
                case IN_PROGRESS -> "in-progress";
                case COMPLETED -> "completed";
            };
        }
    }

    public enum TaskPriority {
        LOW, MEDIUM, HIGH;

        public static TaskPriority fromApiString(String s) {
            if (s == null) return MEDIUM;
            return switch (s) {
                case "low" -> LOW;
                case "medium" -> MEDIUM;
                case "high" -> HIGH;
                default -> MEDIUM;
            };
        }

        public String toApiString() {
            return switch (this) {
                case LOW -> "low";
                case MEDIUM -> "medium";
                case HIGH -> "high";
            };
        }
    }

    public Task() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
