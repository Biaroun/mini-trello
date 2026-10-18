package com.example.minitrello.card.dto;

import com.example.minitrello.card.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCardRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 1000) String description,
        Priority priority,
        LocalDate dueDate
) {
}
