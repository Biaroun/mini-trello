package com.example.minitrello.column;
import com.example.minitrello.board.Board;
import com.example.minitrello.board.BoardRepository;
import com.example.minitrello.column.dto.ColumnResponse;
import com.example.minitrello.column.dto.CreateColumnRequest;
import com.example.minitrello.column.dto.UpdateColumnRequest;
import com.example.minitrello.common.exception.ConflictException;
import com.example.minitrello.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardColumnService {

    private final BoardColumnRepository boardColumnRepository;
    private final BoardColumnMapper boardColumnMapper;
    private final BoardRepository boardRepository;

    public BoardColumnService(BoardColumnRepository boardColumnRepository, BoardColumnMapper boardColumnMapper, BoardRepository boardRepository) {
        this.boardColumnRepository = boardColumnRepository;
        this.boardColumnMapper = boardColumnMapper;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public ColumnResponse create(Long boardId, CreateColumnRequest request) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ResourceNotFoundException("Board " + boardId + " introuvable"));
        if (boardColumnRepository.existsByBoardIdAndName(boardId, request.name())) {
            throw new ConflictException("Une colonne nommée '" + request.name() + "' existe déjà dans ce tableau");
        }
        int position = (int) boardColumnRepository.countByBoardId(boardId);
        BoardColumn boardColumn = new BoardColumn(request.name(), board, position);
        BoardColumn saved = boardColumnRepository.save(boardColumn);
        return boardColumnMapper.toColumnResponse(saved);
    }

    @Transactional
    public ColumnResponse rename(Long id, UpdateColumnRequest request) {
        BoardColumn boardColumn = getColumnOrThrow(id);
        if (boardColumnRepository.existsByBoardIdAndNameAndIdNot(
                boardColumn.getBoard().getId(), request.name(), id)) {
            throw new ConflictException("Une colonne nommée '" + request.name() + "' existe déjà dans ce tableau");
        }
        boardColumn.setName(request.name());
        return boardColumnMapper.toColumnResponse(boardColumn);
    }

    @Transactional
    public void delete(Long id) {
        BoardColumn boardColumn = getColumnOrThrow(id);
        boardColumnRepository.delete(boardColumn);
    }

    private BoardColumn getColumnOrThrow(Long id) {
        return boardColumnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Column " + id + " introuvable"));
    }
}
