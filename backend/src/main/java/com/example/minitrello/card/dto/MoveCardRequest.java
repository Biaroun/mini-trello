package com.example.minitrello.card.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveCardRequest(
        @NotNull Long targetColumnId,
        @NotNull @PositiveOrZero Integer position
) {
}
