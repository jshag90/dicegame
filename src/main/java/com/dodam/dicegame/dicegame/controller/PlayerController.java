package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.exception.SameAlreadyNickNamePlayerException;
import com.dodam.dicegame.dicegame.exception.SameNickNamePlayerException;
import com.dodam.dicegame.dicegame.service.PlayerService;
import com.dodam.dicegame.dicegame.util.ReturnCode;
import com.dodam.dicegame.dicegame.vo.ReturnCodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
@Slf4j
@Tag(name = "player API", description = "플레이어 관련 API")
@CrossOrigin(origins = "*", maxAge = 3600) // 모든 출처 허용, 캐시 시간 설정
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PutMapping("/update/nick-name/{playerId}/{nickName}")
    @Operation(summary = "플레이어 닉네임 변경", description = "플레이어의 닉네임을 변경합니다.")
    public ResponseEntity<ReturnCodeVO<Void>> updateNickName(
            @PathVariable("playerId") Long playerId,
            @PathVariable("nickName") String nickName) throws SameAlreadyNickNamePlayerException, SameNickNamePlayerException {

        playerService.updatePlayerNickName(playerId, nickName);

        return ResponseEntity.ok(ReturnCodeVO.<Void>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .build());
    }

}
