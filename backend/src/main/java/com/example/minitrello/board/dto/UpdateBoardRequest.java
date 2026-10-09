package com.example.minitrello.board.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBoardRequest(
        @NotBlank @Size(max = 100) String name
) {}
