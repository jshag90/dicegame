package com.dodam.dicegame.dicegame.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum ReturnCode {
    SUCCESS(0),
    NO_EXIST_ROOM(-2),
    TOO_MANY_PLAYER(-3),
    NO_EXIST_PUBLIC_ROOM(-4),
    ALREADY_USED_NICK_NAME(-5)
    ;

    private final Integer value;
}
