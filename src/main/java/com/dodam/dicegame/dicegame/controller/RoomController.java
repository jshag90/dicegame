package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.dto.RoomPlayerInfo;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.exception.TooManyPlayerException;
import com.dodam.dicegame.dicegame.service.RoomService;
import com.dodam.dicegame.dicegame.util.ReturnCode;
import com.dodam.dicegame.dicegame.vo.JoinRoomPlayerVO;
import com.dodam.dicegame.dicegame.vo.ReturnCodeVO;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
import com.dodam.dicegame.dicegame.vo.RoomSettingInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@Slf4j
@Tag(name = "Room API", description = "방(Room) 관련 API")
@CrossOrigin(origins = "*", maxAge = 3600) // 모든 출처 허용, 캐시 시간 설정
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    @Operation(summary = "방 생성", description = "방생성 정보를 저장합니다.")
    public ResponseEntity<ReturnCodeVO<Long>> createRoom(@RequestBody RoomInfoVO roomInfoVO) {
        Long roomId = roomService.createRoom(roomInfoVO);
        return ResponseEntity.ok(ReturnCodeVO.<Long>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .data(roomId)
                .build());
    }

    @PostMapping("/secret/join")
    @Operation(summary = "비공개방 입장하기", description = "방에 들어가기 위한 사용자를 저장합니다.")
    public ResponseEntity<ReturnCodeVO<Long>> joinRoomPlayer(
            @RequestBody JoinRoomPlayerVO joinRoomPlayerVO) throws TooManyPlayerException, NoExistRoomException {
        Long playerId = roomService.joinSecretRoomPlayer(joinRoomPlayerVO);
        return ResponseEntity.ok(ReturnCodeVO.<Long>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .data(playerId)
                .build());
    }

    @GetMapping("/public/join/nick_name={nickName}")
    @Operation(summary = "공개방 입장하기", description = "방에 들어가기 위한 사용자를 저장합니다.")
    public ResponseEntity<ReturnCodeVO<RoomPlayerInfo>> joinPublicRoomPlayer(@PathVariable("nickName") String nickName) {
        int returnCode = roomService.checkJoinPublicRoomPlayer(nickName).intValue();
        if (returnCode < 0) {
            return ResponseEntity.ok(ReturnCodeVO.<RoomPlayerInfo>builder().returnCode(returnCode).build());
        }

        RoomPlayerInfo roomPlayerInfo = roomService.handleJoinPublicRoomPlayer(Long.valueOf(returnCode), nickName);
        return ResponseEntity.ok(ReturnCodeVO.<RoomPlayerInfo>builder()
                            .returnCode(returnCode)
                            .data(roomPlayerInfo)
                            .build());
    }

    @GetMapping("/remove/room_id={roomId}")
    @Operation(summary = "방 제거하기", description = "게임이 종료되면 방을 제거합니다.")
    public ResponseEntity<ReturnCodeVO<Void>> removeRoom(@PathVariable("roomId") Long roomId) {
        roomService.removeRoom(roomId);
        return ResponseEntity.ok(ReturnCodeVO.<Void>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .build());
    }

    @GetMapping("/info/room_id={roomId}")
    @Operation(summary = "방 설정 정보", description = "방 설정 정보를 가져옵니다.")
    public ResponseEntity<ReturnCodeVO<RoomSettingInfoVO>> getRoomInfo(@PathVariable("roomId") Long roomId) throws NoExistRoomException {
        return ResponseEntity.ok(ReturnCodeVO.<RoomSettingInfoVO>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .data(roomService.getRoomSettingInfo(roomId))
                .build());
    }
}
