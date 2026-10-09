package com.example.minitrello.board.dto;

import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {
}
