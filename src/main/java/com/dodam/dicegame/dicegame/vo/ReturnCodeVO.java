package com.dodam.dicegame.dicegame.vo;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReturnCodeVO<T> {
    Integer returnCode;
    T data;
}
