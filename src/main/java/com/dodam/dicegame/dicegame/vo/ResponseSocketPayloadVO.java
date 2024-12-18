package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ResponseSocketPayloadVO {
    String action;
    String message;
    String subMessage;
}
