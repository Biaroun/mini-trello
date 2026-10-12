package com.example.minitrello.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    long countByColumnId(Long columnId);

    List<Card> findByColumnIdOrderByPositionAsc(Long columnId);
}
