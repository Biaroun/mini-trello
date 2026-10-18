package com.example.minitrello.board;
import com.example.minitrello.board.dto.BoardDetailResponse;
import com.example.minitrello.board.dto.BoardResponse;
import com.example.minitrello.board.dto.CreateBoardRequest;
import com.example.minitrello.board.dto.UpdateBoardRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping()
    public List<BoardResponse> findAll() {
        return boardService.findAll();
    }

    @GetMapping("{id}")
    public BoardDetailResponse findById(@PathVariable Long id) {
        return boardService.findById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    public BoardResponse create(@Valid @RequestBody CreateBoardRequest request) {
        return boardService.create(request);
    }

    @PutMapping("{id}")
    public BoardResponse update(@PathVariable Long id, @Valid @RequestBody UpdateBoardRequest request) {
        return boardService.rename(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        boardService.delete(id);
    }


}
