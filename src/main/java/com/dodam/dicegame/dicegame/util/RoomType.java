package com.dodam.dicegame.dicegame.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum RoomType {
    PUBLIC("public"),
    SECRET("secret");

    private final String value;
}
