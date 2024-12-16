package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.dto.ScoreResults;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.service.ScoreService;
import com.dodam.dicegame.dicegame.util.ReturnCode;
import com.dodam.dicegame.dicegame.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/score")
@Slf4j
@Tag(name = "Score API", description = "점수(Room) 관련 API")
@CrossOrigin(origins = "*", maxAge = 3600) // 모든 출처 허용, 캐시 시간 설정
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/save")
    @Operation(summary = "게임 점수 저장", description = "게임 점수를 저장합니다.")
    public ResponseEntity<ReturnCodeVO<Long>> saveGameScore(@RequestBody SaveScoreVO saveScoreVO) {
        log.info("/score/save {}",saveScoreVO);
        scoreService.saveScore(saveScoreVO);
        return ResponseEntity.ok(ReturnCodeVO.<Long>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .build());
    }

    @GetMapping("/results/room-id={roomId}")
    @Operation(summary = "게임 결과 조회", description = "플레이어 게임 결과 순위 조회")
    public ResponseEntity<ReturnCodeVO<List<ScoreResults>>> getGameScoreResults(@PathVariable("roomId") Long roomId) throws NoExistRoomException, InterruptedException {
        log.info("/results/room-id {}", roomId);
        return ResponseEntity.ok(ReturnCodeVO.<List<ScoreResults>>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .data(scoreService.getGameScoreResults(roomId))
                .build());
    }
}
