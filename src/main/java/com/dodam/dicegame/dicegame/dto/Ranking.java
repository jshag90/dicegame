package com.dodam.dicegame.dicegame.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Ranking {
    Integer rank;
    Integer totalScore;
    String uuid;

}
