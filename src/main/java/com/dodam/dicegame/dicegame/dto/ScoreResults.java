package com.dodam.dicegame.dicegame.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResults {
    Integer rank;
    Integer score;
    String nickName;
    Integer roomId;
    Integer targetNumber;
}
