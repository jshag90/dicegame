package com.dodam.dicegame.dicegame.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerInfo {

    private String uuid;

    private LocalDateTime createdAt;

    private String isManager;

    private Long roomId;

    private int totalScore;
}
