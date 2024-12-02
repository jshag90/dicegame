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
    TOO_MANY_PLAYER(-3);

    private final Integer value;
}
