package com.example.minitrello.board;
import com.example.minitrello.board.dto.BoardResponse;
import com.example.minitrello.board.dto.CreateBoardRequest;
import org.springframework.stereotype.Component;


@Component
public class BoardMapper {

    public BoardResponse toResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getCreatedAt()
        );
    }

    public Board toEntity(CreateBoardRequest request) {
        return new Board(
                request.name()
        );
    }


}
