package com.dodam.dicegame.dicegame.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveScoreVO {
    Long roomId;
    Long playerId;
    Integer round;
    Integer score;
    String isGo;
}
