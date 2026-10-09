package com.example.minitrello.board;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Board() {
    }
    public Board(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }
    public String getName() {
        return  name;
    }
    public LocalDateTime getCreatedAt() {
        return   createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

}

