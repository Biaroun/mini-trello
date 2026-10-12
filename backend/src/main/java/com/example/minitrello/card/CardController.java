package com.example.minitrello.card;

import com.example.minitrello.card.dto.CardResponse;
import com.example.minitrello.card.dto.CreateCardRequest;
import com.example.minitrello.card.dto.MoveCardRequest;
import com.example.minitrello.card.dto.UpdateCardRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/columns/{columnId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@PathVariable Long columnId,
                               @Valid @RequestBody CreateCardRequest request) {
        return cardService.create(columnId, request);
    }

    @PutMapping("/cards/{id}")
    public CardResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateCardRequest request) {
        return cardService.update(id, request);
    }

    @PatchMapping("/cards/{id}/move")
    public CardResponse move(@PathVariable Long id,
                             @Valid @RequestBody MoveCardRequest request) {
        return cardService.move(id, request);
    }

    @DeleteMapping("/cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cardService.delete(id);
    }
}
