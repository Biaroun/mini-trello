package com.example.minitrello.column;

import com.example.minitrello.column.dto.ColumnResponse;
import com.example.minitrello.column.dto.CreateColumnRequest;
import com.example.minitrello.column.dto.UpdateColumnRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BoardColumnController {

    private final BoardColumnService boardColumnService;

    public BoardColumnController(BoardColumnService boardColumnService) {
        this.boardColumnService = boardColumnService;
    }

    @PostMapping("/boards/{boardId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    public ColumnResponse create(@PathVariable Long boardId,
                                 @Valid @RequestBody CreateColumnRequest request) {
        return boardColumnService.create(boardId, request);
    }

    @PutMapping("/columns/{id}")
    public ColumnResponse rename(@PathVariable Long id,
                                 @Valid @RequestBody UpdateColumnRequest request) {
        return boardColumnService.rename(id, request);
    }

    @DeleteMapping("/columns/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        boardColumnService.delete(id);
    }
}
