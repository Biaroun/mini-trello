package com.example.minitrello.column.dto;

public record ColumnResponse(
        Long id,
        String name,
        int position,
        Long boardId
) {
}
