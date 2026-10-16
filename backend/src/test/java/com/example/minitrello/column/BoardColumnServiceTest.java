package com.example.minitrello.column;

import com.example.minitrello.board.Board;
import com.example.minitrello.board.BoardRepository;
import com.example.minitrello.column.dto.CreateColumnRequest;
import com.example.minitrello.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardColumnServiceTest {

    @Mock
    private BoardColumnRepository boardColumnRepository;
    @Mock
    private BoardColumnMapper boardColumnMapper;
    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardColumnService boardColumnService;

    @Test
    void create_nomDejaPrisDansLeTableau_lanceConflictException() {
        // Given : le tableau existe et contient déjà une colonne "À faire"
        when(boardRepository.findById(1L)).thenReturn(Optional.of(new Board("Projet")));
        when(boardColumnRepository.existsByBoardIdAndName(1L, "À faire")).thenReturn(true);

        // When + Then : la création est refusée
        assertThrows(ConflictException.class,
                () -> boardColumnService.create(1L, new CreateColumnRequest("À faire")));

        // Et rien n'a été enregistré
        verify(boardColumnRepository, never()).save(any());
    }
}
