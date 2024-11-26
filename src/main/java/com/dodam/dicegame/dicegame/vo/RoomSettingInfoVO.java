package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RoomSettingInfoVO {
    int diceCount;
    int maxPlayers;
    String roomName;
    String roomType;
    int targetNumber;

}
