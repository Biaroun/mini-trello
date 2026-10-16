package com.example.minitrello.board.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoardDetailResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        List<ColumnDetailResponse> columns
) {
}
