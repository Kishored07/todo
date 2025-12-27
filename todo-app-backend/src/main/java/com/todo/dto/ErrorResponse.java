package com.todo.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String details,
        int status,
        LocalDateTime timestamp
) {}
