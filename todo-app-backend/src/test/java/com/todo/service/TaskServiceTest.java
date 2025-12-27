package com.todo.service;

import com.todo.dto.TaskRequest;
import com.todo.entity.Task;
import com.todo.entity.User;
import com.todo.repository.TaskRepository;
import com.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService service;

    @Test
    void createTask_ShouldCreateAndReturnTask() {
        // ---------- Arrange ----------
        User mockUser = User.builder()
                .id(1L)
                .username("test")
                .build();

        Task savedTask = Task.builder()
                .id(1L)
                .title("Test")
                .user(mockUser)
                .build();

        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(mockUser));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        // ✅ Proper SecurityContext mocking
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // ---------- Act ----------
        var response = service.createTask(new TaskRequest("Test", "Desc"));

        // ---------- Assert ----------
        assertEquals("Test", response.title());
        verify(taskRepository).save(any(Task.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllTasks_ShouldReturnPaginatedTasks() {
        // ---------- Arrange ----------
        User mockUser = User.builder()
                .id(1L)
                .username("test")
                .build();

        List<Task> tasks = List.of(
                Task.builder()
                        .title("Task1")
                        .user(mockUser)
                        .build()
        );

        PageRequest pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(mockUser));

        when(taskRepository.findByUser(mockUser, pageable))
                .thenReturn(new PageImpl<>(tasks));

        // ✅ Proper SecurityContext mocking
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        // ---------- Act ----------
        var page = service.getAllTasks(pageable);

        // ---------- Assert ----------
        assertEquals(1, page.getTotalElements());

        SecurityContextHolder.clearContext();
    }
}
