package com.example.minitrello.column.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateColumnRequest(
        @NotBlank @Size(max = 50) String name
) {
}
