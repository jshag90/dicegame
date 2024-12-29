package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class JoinRoomPlayerVO {
    Long roomId;
    String entryCode;
    String uuid;
}
