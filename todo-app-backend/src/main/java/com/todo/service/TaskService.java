package com.todo.service;

import com.todo.dto.TaskRequest;
import com.todo.dto.TaskResponse;
import com.todo.entity.Task;
import com.todo.entity.User;
import com.todo.exception.ResourceNotFoundException;
import com.todo.exception.UnauthorizedException;
import com.todo.repository.TaskRepository;
import com.todo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        User user = getCurrentUser();
        Page<Task> tasks = taskRepository.findByUser(user, pageable);
        return tasks.map(this::toResponse);
    }

    public TaskResponse getTask(Long id) {
        Task task = findTask(id);
        checkOwnership(task);
        return toResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {
        User user = getCurrentUser();
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .user(user)
                .build();
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);
        checkOwnership(task);
        task.setTitle(request.title());
        task.setDescription(request.description());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse toggleComplete(Long id) {
        Task task = findTask(id);
        checkOwnership(task);
        task.setCompleted(!task.isCompleted());
        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        Task task = findTask(id);
        checkOwnership(task);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private void checkOwnership(Task task) {
        if (!task.getUser().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedException("You are not authorized to access this task");
        }
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt()
        );
    }
}
