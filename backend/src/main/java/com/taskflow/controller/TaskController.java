package com.taskflow.controller;

import com.taskflow.dto.TaskListResponse;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.model.User;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<TaskListResponse> getAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search) {
        TaskListResponse response = taskService.getAll(user.getId(), status, priority, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, TaskResponse>> getById(@AuthenticationPrincipal User user,
                                                             @PathVariable Long id) {
        TaskResponse response = taskService.getById(user.getId(), id);
        return ResponseEntity.ok(Map.of("task", response));
    }

    @PostMapping
    public ResponseEntity<Map<String, TaskResponse>> create(@AuthenticationPrincipal User user,
                                                            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("task", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, TaskResponse>> update(@AuthenticationPrincipal User user,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.update(user.getId(), id, request);
        return ResponseEntity.ok(Map.of("task", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@AuthenticationPrincipal User user,
                                                      @PathVariable Long id) {
        taskService.delete(user.getId(), id);
        return ResponseEntity.ok(Map.of(
                "message", "Task deleted successfully",
                "_id", String.valueOf(id)
        ));
    }
}
