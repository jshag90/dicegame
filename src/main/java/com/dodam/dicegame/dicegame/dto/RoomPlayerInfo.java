package com.dodam.dicegame.dicegame.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayerInfo {
    int targetNumber;
    int diceCount;
    Long playerId;
    String nickName;
    Long roomId;
    int maxPlayer;
}
