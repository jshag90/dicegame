package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SocketPayloadVO {
    String action;
    String roomId;
    String nickName;
    String isGo;
  /*  String round;
    String isGo;*/
}
