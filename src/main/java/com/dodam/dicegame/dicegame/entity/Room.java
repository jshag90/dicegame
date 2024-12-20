package com.dodam.dicegame.dicegame.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int maxPlayers;

    private int targetNumber;

    private int diceCount;

    private String roomType;

    private String entryCode;

    @Column(columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String isGameStarted;
}
