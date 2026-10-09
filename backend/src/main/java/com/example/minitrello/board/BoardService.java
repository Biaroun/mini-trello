package com.example.minitrello.board;
import com.example.minitrello.board.dto.BoardResponse;
import com.example.minitrello.board.dto.CreateBoardRequest;
import com.example.minitrello.board.dto.UpdateBoardRequest;
import com.example.minitrello.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;

    public BoardService(BoardRepository boardRepository, BoardMapper boardMapper) {
        this.boardRepository = boardRepository;
        this.boardMapper = boardMapper;
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> findAll() {
        return boardRepository.findAll().stream()
                .map(boardMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse findById(Long id) {
        return boardMapper.toResponse(getBoardOrThrow(id));
    }

    @Transactional
    public BoardResponse create(CreateBoardRequest request) {
        Board board = boardMapper.toEntity(request);
        Board saved = boardRepository.save(board);
        return boardMapper.toResponse(saved);
    }

    @Transactional
    public BoardResponse rename(Long id, UpdateBoardRequest request) {
        Board board = getBoardOrThrow(id);
        board.setName(request.name());
        return boardMapper.toResponse(board);
    }

    @Transactional
    public void delete(Long id){
        Board board = getBoardOrThrow(id);
        boardRepository.delete(board);
    }

    private Board getBoardOrThrow(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board " + id + " introuvable"));
    }
}