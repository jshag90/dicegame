package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.exception.TooManyPlayerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class AdviceController {

    @ExceptionHandler(NoExistRoomException.class)
    public ResponseEntity<Long> NoExistRoomException(NoExistRoomException nere) {
        log.info(nere.getMessage());
        return new ResponseEntity<>(-2L, HttpStatus.OK);
    }

    @ExceptionHandler(TooManyPlayerException.class)
    public ResponseEntity<Long> handleTooManyPlayerException(TooManyPlayerException tmp) {
        log.info(tmp.getMessage());
        return new ResponseEntity<>(-3L, HttpStatus.OK);
    }


}
