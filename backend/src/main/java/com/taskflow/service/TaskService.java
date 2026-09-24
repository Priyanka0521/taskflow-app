package com.taskflow.service;

import com.taskflow.dto.TaskListResponse;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Task;
import com.taskflow.model.Task.TaskPriority;
import com.taskflow.model.Task.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskListResponse getAll(Long userId, String status, String priority, String search) {
        User user = userRepository.getReferenceById(userId);
        TaskStatus ts = status == null || status.isBlank() ? null : TaskStatus.fromApiString(status);
        TaskPriority tp = priority == null || priority.isBlank() ? null : TaskPriority.fromApiString(priority);
        String searchTerm = (search == null || search.isBlank()) ? null : search;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Task> tasks = taskRepository.findByUserFiltered(user, ts, tp, searchTerm, sort);

        List<TaskResponse> items = tasks.stream().map(TaskResponse::new).toList();
        return new TaskListResponse(items);
    }

    public TaskResponse getById(Long userId, Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return new TaskResponse(task);
    }

    public TaskResponse create(Long userId, TaskRequest request) {
        User user = userRepository.getReferenceById(userId);
        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        task.setStatus(TaskStatus.fromApiString(request.getStatus()));
        task.setPriority(TaskPriority.fromApiString(request.getPriority()));
        task.setDueDate(request.getDueDate());
        Task saved = taskRepository.save(task);
        return new TaskResponse(saved);
    }

    public TaskResponse update(Long userId, Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.fromApiString(request.getStatus()));
        }
        if (request.getPriority() != null) {
            task.setPriority(TaskPriority.fromApiString(request.getPriority()));
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        } else if (request.getDueDate() == null && hasNullDueDateField(request)) {
            task.setDueDate(null);
        }
        Task saved = taskRepository.save(task);
        return new TaskResponse(saved);
    }

    private boolean hasNullDueDateField(TaskRequest request) {
        try {
            return request.getClass().getDeclaredField("dueDate") != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public void delete(Long userId, Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        taskRepository.delete(task);
    }
}
