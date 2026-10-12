package com.example.minitrello.column;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {

    boolean existsByBoardIdAndName(Long boardId, String name);
    long countByBoardId(Long boardId);
    boolean existsByBoardIdAndNameAndIdNot(Long boardId, String name, Long id);
}
