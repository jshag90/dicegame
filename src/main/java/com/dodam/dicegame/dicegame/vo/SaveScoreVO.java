package com.dodam.dicegame.dicegame.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaveScoreVO {
    Long roomId;
    String nickName;
    Integer finalRound;
    Integer score;
}
