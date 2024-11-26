package com.dodam.dicegame.dicegame.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum RoomManager {
    MANAGER("Y"),
    NORMAL("N");

    private final String value;
}
