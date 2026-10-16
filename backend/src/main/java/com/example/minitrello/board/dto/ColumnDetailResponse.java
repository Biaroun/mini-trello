package com.example.minitrello.board.dto;

import com.example.minitrello.card.dto.CardResponse;

import java.util.List;

public record ColumnDetailResponse(
        Long id,
        String name,
        int position,
        List<CardResponse> cards
) {
}
