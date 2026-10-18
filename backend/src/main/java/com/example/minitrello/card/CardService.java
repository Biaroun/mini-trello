package com.example.minitrello.card;

import com.example.minitrello.card.dto.CardResponse;
import com.example.minitrello.card.dto.CreateCardRequest;
import com.example.minitrello.card.dto.MoveCardRequest;
import com.example.minitrello.card.dto.UpdateCardRequest;
import com.example.minitrello.column.BoardColumn;
import com.example.minitrello.column.BoardColumnRepository;
import com.example.minitrello.common.exception.BadRequestException;
import com.example.minitrello.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, BoardColumnRepository boardColumnRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.cardMapper = cardMapper;
    }

    @Transactional
    public CardResponse create(Long columnId, CreateCardRequest request) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column " + columnId + " introuvable"));

        if (request.dueDate() != null && request.dueDate().isBefore(LocalDate.now())){
            throw new BadRequestException("La date d'échéance ne peut pas être dans le passé");
        }

        int position = (int) cardRepository.countByColumnId(columnId);
        Card card = new Card(request.title(), request.description(), request.priority(), column, position, request.dueDate());
        Card saved = cardRepository.save(card);
        return cardMapper.toResponse(saved);
    }

    @Transactional
    public CardResponse update(Long id, UpdateCardRequest request) {
        Card card = getCardOrThrow(id);
        card.setTitle(request.title());
        card.setDescription(request.description());
        card.setPriority(request.priority());
        card.setDueDate(request.dueDate());
        return cardMapper.toResponse(card);
    }

    @Transactional
    public CardResponse move(Long id, MoveCardRequest request) {
        Card card = getCardOrThrow(id);
        BoardColumn sourceColumn = card.getColumn();
        BoardColumn targetColumn = boardColumnRepository.findById(request.targetColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Column " + request.targetColumnId() + " introuvable"));

        // Une carte ne peut être déplacée que vers une colonne du même tableau
        if (!targetColumn.getBoard().getId().equals(sourceColumn.getBoard().getId())) {
            throw new BadRequestException("Impossible de déplacer une carte vers une colonne d'un autre tableau");
        }

        List<Card> sourceCards = new ArrayList<>(cardRepository.findByColumnIdOrderByPositionAsc(sourceColumn.getId()));
        sourceCards.remove(card);

        boolean sameColumn = sourceColumn.getId().equals(targetColumn.getId());
        List<Card> targetCards = sameColumn
                ? sourceCards
                : new ArrayList<>(cardRepository.findByColumnIdOrderByPositionAsc(targetColumn.getId()));

        // Une position trop grande place la carte en fin de colonne
        int position = Math.min(request.position(), targetCards.size());
        targetCards.add(position, card);
        card.setColumn(targetColumn);

        // Les positions doivent rester continues (0, 1, 2…) dans les deux colonnes
        renumber(sourceCards);
        if (!sameColumn) {
            renumber(targetCards);
        }

        return cardMapper.toResponse(card);
    }

    @Transactional
    public void delete(Long id) {
        Card card = getCardOrThrow(id);
        Long columnId = card.getColumn().getId();
        cardRepository.delete(card);

        // On referme le trou laissé par la carte supprimée
        List<Card> remaining = new ArrayList<>(cardRepository.findByColumnIdOrderByPositionAsc(columnId));
        remaining.remove(card);
        renumber(remaining);
    }

    private void renumber(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }
    }

    private Card getCardOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card " + id + " introuvable"));
    }
}
