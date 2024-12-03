package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.exception.SameAlreadyNickNamePlayerException;
import com.dodam.dicegame.dicegame.exception.SameNickNamePlayerException;
import com.dodam.dicegame.dicegame.exception.TooManyPlayerException;
import com.dodam.dicegame.dicegame.util.ReturnCode;
import com.dodam.dicegame.dicegame.vo.ReturnCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class AdviceController {

    @ExceptionHandler(NoExistRoomException.class)
    public ResponseEntity<ReturnCodeVO> handleNoExistRoomException(NoExistRoomException nere) {
        log.info(nere.getMessage());
        return new ResponseEntity<>(ReturnCodeVO.builder().returnCode(ReturnCode.NO_EXIST_ROOM.getValue()).build(), HttpStatus.OK);
    }

    @ExceptionHandler(TooManyPlayerException.class)
    public ResponseEntity<ReturnCodeVO> handleTooManyPlayerException(TooManyPlayerException tmp) {
        log.info(tmp.getMessage());
        return new ResponseEntity<>(ReturnCodeVO.builder().returnCode(ReturnCode.TOO_MANY_PLAYER.getValue()).build(), HttpStatus.OK);
    }

    @ExceptionHandler(SameNickNamePlayerException.class)
    public ResponseEntity<ReturnCodeVO> handleSameNickNameException(SameNickNamePlayerException sameNickNamePlayerException) {
        log.info(sameNickNamePlayerException.getMessage());
        return new ResponseEntity<>(ReturnCodeVO.builder().returnCode(ReturnCode.ALREADY_USED_NICK_NAME.getValue()).build(), HttpStatus.OK);
    }

    @ExceptionHandler(SameAlreadyNickNamePlayerException.class)
    public ResponseEntity<ReturnCodeVO> handleSameAlreadyNickNameException(SameAlreadyNickNamePlayerException sameAlreadyNickNamePlayerException) {
        log.info(sameAlreadyNickNamePlayerException.getMessage());
        return new ResponseEntity<>(ReturnCodeVO.builder().returnCode(ReturnCode.SAME_ALREADY_USED_NICK_NAME.getValue()).build(), HttpStatus.OK);
    }


}
