package com.example.minitrello.card;

import com.example.minitrello.board.Board;
import com.example.minitrello.card.dto.CreateCardRequest;
import com.example.minitrello.card.dto.MoveCardRequest;
import com.example.minitrello.column.BoardColumn;
import com.example.minitrello.column.BoardColumnRepository;
import com.example.minitrello.common.exception.BadRequestException;
import com.example.minitrello.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private BoardColumnRepository boardColumnRepository;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    // ---------- create ----------

    @Test
    void create_ajouteLaCarteALaFinDeLaColonne() {
        BoardColumn column = column(1L, board(1L));
        when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(column));
        when(cardRepository.countByColumnId(1L)).thenReturn(3L);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.create(1L, new CreateCardRequest("Nouvelle carte", null, null, null));

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        assertEquals(3, captor.getValue().getPosition());
        assertEquals(Priority.MEDIUM, captor.getValue().getPriority());
    }

    @Test
    void create_dateEcheanceDansLePasse_lanceBadRequestException() {
        when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(column(1L, board(1L))));
        CreateCardRequest request = new CreateCardRequest("Carte", null, null, LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> cardService.create(1L, request));

        verify(cardRepository, never()).save(any());
    }

    @Test
    void create_dateEcheanceAujourdhui_estAcceptee() {
        when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(column(1L, board(1L))));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.create(1L, new CreateCardRequest("Carte", null, null, LocalDate.now()));

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void create_colonneIntrouvable_lanceResourceNotFoundException() {
        when(boardColumnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cardService.create(99L, new CreateCardRequest("Carte", null, null, null)));
    }

    // ---------- move ----------

    @Test
    void move_versUneAutreColonne_reordonneLesPositionsDesDeuxColonnes() {
        Board board = board(1L);
        BoardColumn source = column(1L, board);
        BoardColumn target = column(2L, board);
        Card a = card(10L, "A", source, 0);
        Card b = card(11L, "B", source, 1);
        Card c = card(12L, "C", source, 2);
        Card x = card(20L, "X", target, 0);
        Card y = card(21L, "Y", target, 1);

        when(cardRepository.findById(11L)).thenReturn(Optional.of(b));
        when(boardColumnRepository.findById(2L)).thenReturn(Optional.of(target));
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(a, b, c));
        when(cardRepository.findByColumnIdOrderByPositionAsc(2L)).thenReturn(List.of(x, y));

        // B passe de la colonne 1 a la colonne 2, en position 1
        cardService.move(11L, new MoveCardRequest(2L, 1));

        // colonne source : A, C
        assertEquals(0, a.getPosition());
        assertEquals(1, c.getPosition());
        // colonne cible : X, B, Y
        assertEquals(0, x.getPosition());
        assertEquals(1, b.getPosition());
        assertEquals(2, y.getPosition());
        assertSame(target, b.getColumn());
    }

    @Test
    void move_dansLaMemeColonne_reordonneLesPositions() {
        BoardColumn column = column(1L, board(1L));
        Card a = card(10L, "A", column, 0);
        Card b = card(11L, "B", column, 1);
        Card c = card(12L, "C", column, 2);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(a));
        when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(column));
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(a, b, c));

        // A passe tout en bas : B, C, A
        cardService.move(10L, new MoveCardRequest(1L, 2));

        assertEquals(0, b.getPosition());
        assertEquals(1, c.getPosition());
        assertEquals(2, a.getPosition());
    }

    @Test
    void move_positionTropGrande_placeLaCarteEnFinDeColonne() {
        BoardColumn column = column(1L, board(1L));
        Card a = card(10L, "A", column, 0);
        Card b = card(11L, "B", column, 1);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(a));
        when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(column));
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(a, b));

        cardService.move(10L, new MoveCardRequest(1L, 99));

        assertEquals(0, b.getPosition());
        assertEquals(1, a.getPosition());
    }

    @Test
    void move_versUneColonneDUnAutreTableau_lanceBadRequestException() {
        BoardColumn source = column(1L, board(1L));
        BoardColumn target = column(2L, board(2L));
        Card a = card(10L, "A", source, 0);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(a));
        when(boardColumnRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(BadRequestException.class, () -> cardService.move(10L, new MoveCardRequest(2L, 0)));

        // la carte n'a pas bouge
        assertSame(source, a.getColumn());
        assertEquals(0, a.getPosition());
    }

    @Test
    void move_carteIntrouvable_lanceResourceNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.move(99L, new MoveCardRequest(1L, 0)));
    }

    // ---------- delete ----------

    @Test
    void delete_refermeLeTrouDansLaColonne() {
        BoardColumn column = column(1L, board(1L));
        Card a = card(10L, "A", column, 0);
        Card b = card(11L, "B", column, 1);
        Card c = card(12L, "C", column, 2);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(a));
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(b, c));

        cardService.delete(10L);

        verify(cardRepository).delete(a);
        assertEquals(0, b.getPosition());
        assertEquals(1, c.getPosition());
    }

    // ---------- helpers : les entites n'ont pas de setter pour l'id ----------

    private Board board(Long id) {
        Board board = new Board("Tableau " + id);
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    private BoardColumn column(Long id, Board board) {
        BoardColumn column = new BoardColumn("Colonne " + id, board, 0);
        ReflectionTestUtils.setField(column, "id", id);
        return column;
    }

    private Card card(Long id, String title, BoardColumn column, int position) {
        Card card = new Card(title, null, Priority.MEDIUM, column, position, null);
        ReflectionTestUtils.setField(card, "id", id);
        return card;
    }
}
