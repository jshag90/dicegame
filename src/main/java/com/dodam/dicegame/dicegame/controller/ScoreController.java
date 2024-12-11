package com.dodam.dicegame.dicegame.controller;

import com.dodam.dicegame.dicegame.service.ScoreService;
import com.dodam.dicegame.dicegame.util.ReturnCode;
import com.dodam.dicegame.dicegame.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        scoreService.saveScore(saveScoreVO);
        return ResponseEntity.ok(ReturnCodeVO.<Long>builder()
                .returnCode(ReturnCode.SUCCESS.getValue())
                .build());
    }
}
