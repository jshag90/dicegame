package com.dodam.dicegame.dicegame.vo;

import com.dodam.dicegame.dicegame.util.RoomType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RoomInfoVO {
    int maxPlayers;
    int targetNumber;
    int diceCount;
    RoomType roomType;
    String entryCode;
    String nickName;
}
