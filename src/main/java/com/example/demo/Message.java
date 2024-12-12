package com.example.demo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String room;
    private LocalDateTime timestamp;
    @ManyToOne
    private User sender;
}

