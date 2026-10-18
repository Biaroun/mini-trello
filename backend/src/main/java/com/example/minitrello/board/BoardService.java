package com.example.minitrello.board;
import com.example.minitrello.board.dto.BoardDetailResponse;
import com.example.minitrello.board.dto.BoardResponse;
import com.example.minitrello.board.dto.ColumnDetailResponse;
import com.example.minitrello.board.dto.CreateBoardRequest;
import com.example.minitrello.board.dto.UpdateBoardRequest;
import com.example.minitrello.card.CardMapper;
import com.example.minitrello.card.CardRepository;
import com.example.minitrello.card.dto.CardResponse;
import com.example.minitrello.column.BoardColumnRepository;
import com.example.minitrello.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final BoardColumnRepository boardColumnRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public BoardService(BoardRepository boardRepository, BoardMapper boardMapper,
                        BoardColumnRepository boardColumnRepository, CardRepository cardRepository,
                        CardMapper cardMapper) {
        this.boardRepository = boardRepository;
        this.boardMapper = boardMapper;
        this.boardColumnRepository = boardColumnRepository;
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> findAll() {
        return boardRepository.findAll().stream()
                .map(boardMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardDetailResponse findById(Long id) {
        Board board = getBoardOrThrow(id);

        // 1 requête pour TOUTES les cartes du tableau, regroupées par colonne en mémoire
        Map<Long, List<CardResponse>> cardsByColumnId = cardRepository.findByColumnBoardIdOrderByPositionAsc(id).stream()
                .collect(Collectors.groupingBy(
                        card -> card.getColumn().getId(),
                        Collectors.mapping(cardMapper::toResponse, Collectors.toList())));

        List<ColumnDetailResponse> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(id).stream()
                .map(column -> new ColumnDetailResponse(
                        column.getId(),
                        column.getName(),
                        column.getPosition(),
                        cardsByColumnId.getOrDefault(column.getId(), List.of())))
                .toList();

        return new BoardDetailResponse(board.getId(), board.getName(), board.getCreatedAt(), columns);
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