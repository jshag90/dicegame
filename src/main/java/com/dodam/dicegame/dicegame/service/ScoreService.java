package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.entity.Score;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.repository.ScoreRepository;
import com.dodam.dicegame.dicegame.vo.SaveScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final RoomRepository roomRepository;
    private final ScoreRepository scoreRepository;
    private final PlayerRepository playerRepository;

    public void saveScore(SaveScoreVO saveScoreVO) {

        scoreRepository.save(Score.builder().score(saveScoreVO.getScore())
                .room(roomRepository.findById(saveScoreVO.getRoomId()).get())
                .player(playerRepository.findById(saveScoreVO.getPlayerId()).get())
                .round(saveScoreVO.getRound())
                .build()
        );

    }
}
