package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.entity.Player;
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

    @GetMapping("/save/uuid={uuid}")
    @Operation(summary = "UUID 등록", description = "UUID를 등록합니다.")
    public ResponseEntity<ReturnCodeVO<Void>> deletePlayerInRoom(@PathVariable("uuid") String uuid){
        log.info("/save/uuid={}", uuid);

        playerService.savePlayerUuid(uuid);

        return ResponseEntity.ok(ReturnCodeVO.<Void>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .build());
    }

    @GetMapping("/info/uuid={uuid}")
    @Operation(summary = "플레이어 정보 조회", description = "플레이어 정보를 조회합니다.")
    public ResponseEntity<ReturnCodeVO<Player>> getPlayerInfoByUuid(@PathVariable("uuid") String uuid){
        log.info("/info/uuid={}", uuid);
        return ResponseEntity.ok(ReturnCodeVO.<Player>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .data(playerService.getPlayerInfoByUuid(uuid))
                .build());
    }


    @DeleteMapping("/delete/in-room/{roomId}/{uuid}")
    @Operation(summary = "플레이어를 방에서 제거", description = "플레이어를 방에서 제거합니다.")
    public ResponseEntity<ReturnCodeVO<Void>> deletePlayerInRoom(
            @PathVariable("roomId") String roomId,
            @PathVariable("uuid") String uuid) {

        log.info("/delete/in-room/{}/{}", roomId, uuid);

        playerService.deletePlayerInRoom(Long.valueOf(roomId), uuid);

        return ResponseEntity.ok(ReturnCodeVO.<Void>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .build());
    }

}
