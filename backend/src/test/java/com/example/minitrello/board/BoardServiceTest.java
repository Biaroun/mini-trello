package com.example.minitrello.board;

import com.example.minitrello.board.dto.UpdateBoardRequest;
import com.example.minitrello.card.CardMapper;
import com.example.minitrello.card.CardRepository;
import com.example.minitrello.column.BoardColumnRepository;
import com.example.minitrello.common.exception.ResourceNotFoundException;
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
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private BoardMapper boardMapper;
    @Mock
    private BoardColumnRepository boardColumnRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private BoardService boardService;

    @Test
    void findById_tableauIntrouvable_lanceResourceNotFoundException() {
        when(boardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> boardService.findById(99L));
    }

    @Test
    void rename_tableauIntrouvable_lanceResourceNotFoundException() {
        when(boardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> boardService.rename(99L, new UpdateBoardRequest("Nouveau nom")));
    }

    @Test
    void delete_tableauIntrouvable_lanceExceptionEtNeSupprimeRien() {
        when(boardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> boardService.delete(99L));

        verify(boardRepository, never()).delete(any());
    }
}
