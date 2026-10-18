package com.example.minitrello.card;
import com.example.minitrello.card.dto.CardResponse;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getPriority(),
                card.getDueDate(),
                card.getPosition(),
                card.getColumn().getId(),
                card.getCreatedAt()
        );
    }
}
