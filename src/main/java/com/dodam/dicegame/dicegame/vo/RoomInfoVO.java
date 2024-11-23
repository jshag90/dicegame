package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RoomInfoVO {
    String roomName;
    int maxPlayers;
    int targetNumber;
    int diceCount;
}
