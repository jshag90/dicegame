package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.service.RoomService;
import com.dodam.dicegame.dicegame.vo.JoinRoomPlayerVO;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
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
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    @Operation(summary = "방 생성", description = "방생성 정보를 저장합니다.")
    public ResponseEntity<Long> createRoom(@RequestBody RoomInfoVO roomInfoVO) {
        Long roomId = roomService.createRoom(roomInfoVO);
        return ResponseEntity.ok(roomId);
    }

    @PostMapping("/join")
    @Operation(summary = "방에 입장하기", description = "방에 들어가기 위한 사용자를 저장합니다.")
    public ResponseEntity<Long> joinRoomPlayer(@RequestBody JoinRoomPlayerVO joinRoomPlayerVO) {
        Long playerId = roomService.joinRoomPlayer(joinRoomPlayerVO);
        return ResponseEntity.ok(playerId);
    }
}
