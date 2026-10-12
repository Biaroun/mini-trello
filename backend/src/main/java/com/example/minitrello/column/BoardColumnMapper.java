package com.example.minitrello.column;
import com.example.minitrello.column.dto.ColumnResponse;
import org.springframework.stereotype.Component;

@Component
public class BoardColumnMapper {

    public ColumnResponse toResponse(BoardColumn boardcolumn) {
        return new ColumnResponse(
                boardcolumn.getId(),
                boardcolumn.getName(),
                boardcolumn.getPosition(),
                boardcolumn.getBoard().getId()
        );
    }


}
