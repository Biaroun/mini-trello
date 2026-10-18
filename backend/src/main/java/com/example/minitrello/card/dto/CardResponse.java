package com.example.minitrello.card.dto;

import com.example.minitrello.card.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        String title,
        String description,
        Priority priority,
        LocalDate dueDate,
        int position,
        Long columnId,
        LocalDateTime createdAt
) {
}
