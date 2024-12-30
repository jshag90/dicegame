package com.dodam.dicegame.dicegame.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScoreResults {
    Integer rank;
    Integer score;
    String uuid;
    Integer roomId;
    Integer targetNumber;
}
