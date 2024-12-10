package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ResponseSocketPayloadVO {
    String action;
    String message;
}
