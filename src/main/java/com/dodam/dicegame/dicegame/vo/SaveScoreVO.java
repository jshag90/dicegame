package com.dodam.dicegame.dicegame.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveScoreVO {
    Long roomId;
    String nickName;
    Integer finalRound;
    Integer score;
}
