package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@Slf4j
@Tag(name = "Room API", description = "방(Room) 관련 API")
public class RoomController {

    @PostMapping("/create")
    @Operation(summary = "방 생성", description = "방생성 정보를 저장합니다.")
    public ResponseEntity<Room> createRoom(@RequestBody RoomInfoVO roomInfoVO) {
        log.info(roomInfoVO.toString());
        return (ResponseEntity<Room>) ResponseEntity.ok();
    }
}
