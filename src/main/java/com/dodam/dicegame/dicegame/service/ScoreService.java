package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.dto.Ranking;
import com.dodam.dicegame.dicegame.dto.ScoreResults;
import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.entity.Score;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.repository.ScoreRepository;
import com.dodam.dicegame.dicegame.util.DataUtil;
import com.dodam.dicegame.dicegame.vo.SaveScoreVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreService {

    public static final Map<Long, CountDownLatch> allSaveScoreLatchMap = new ConcurrentHashMap<>();
    private final RoomRepository roomRepository;
    private final ScoreRepository scoreRepository;
    private final PlayerRepository playerRepository;
    private final Map<Integer, Integer> rankToScoreMap = Map.of(
            1, 5,
            2, 3,
            3, 2
    );
    private final AtomicBoolean isSaving = new AtomicBoolean(false);

    @Transactional
    public void saveScore(SaveScoreVO saveScoreVO) {

        if (roomRepository.findById(saveScoreVO.getRoomId()).isPresent()) {

            Room findRoom = roomRepository.findById(saveScoreVO.getRoomId()).get();
            if (isSaving.compareAndSet(false, true)) {
                if (saveScoreVO.getScore() > 0) {
                    saveScoreByExistsScore(saveScoreVO, findRoom);
                }
                isSaving.set(false);
            }

            allSaveScoreLatchMap.putIfAbsent(saveScoreVO.getRoomId(), new CountDownLatch(1));

            if (playerRepository.countByRoom(findRoom) <= scoreRepository.findByRoom(findRoom).size()) {
                allSaveScoreLatchMap.get(saveScoreVO.getRoomId()).countDown();
            }

        }

    }

    private void saveScoreByExistsScore(SaveScoreVO saveScoreVO, Room findRoom) {
        boolean scoreExists = scoreRepository.existsByUuidAndRoomId(saveScoreVO.getUuid(), findRoom.getId());
        if (scoreExists) {
            scoreRepository.updateScoreAndRound(
                    findRoom.getId(),
                    saveScoreVO.getUuid(),
                    saveScoreVO.getFinalRound(),
                    saveScoreVO.getScore()
            );
        } else {
            scoreRepository.save(
                    Score.builder()
                            .score(saveScoreVO.getScore())
                            .room(findRoom)
                            .uuid(saveScoreVO.getUuid())
                            .finalRound(saveScoreVO.getFinalRound())
                            .build()
            );
        }
    }


    @Transactional
    public List<ScoreResults> getGameScoreResults(Long roomId) throws NoExistRoomException, InterruptedException {
        Optional<Room> findRoom = roomRepository.findById(roomId);
        if (findRoom.isEmpty()) {
            throw new NoExistRoomException("해당 roomId가 존재하지 않음 : " + roomId);
        }

        allSaveScoreLatchMap.get(roomId).await();

        List<ScoreResults> scoreResultsList = new ArrayList<>();
        for (Score score : scoreRepository.findByRoom(findRoom.get())) {
            ScoreResults scoreResults = ScoreResults.builder()
                    .uuid(score.getUuid())
                    .score(score.getScore())
                    .roomId(roomId.intValue())
                    .targetNumber(findRoom.get().getTargetNumber())
                    .build();
            scoreResultsList.add(scoreResults);
        }

        // 정렬 및 순위 설정
        DataUtil.sortScoreResults(scoreResultsList);

        // 해당 uuid 승점 증가
        int rank = 1;
        for (ScoreResults scoreResult : scoreResultsList) {
            int plusScore = rankToScoreMap.getOrDefault(rank, -1);
            scoreResult.setRank(rank);
            scoreResult.setPlusTotalScore(plusScore);

            playerRepository.incrementTotalScoreByUuid(scoreResult.getUuid(), plusScore);
            rank++;
        }

        log.info("scoreResultsList() {}", scoreResultsList);
        return scoreResultsList;
    }

    public List<Ranking> getRanking() {
        int limit = 10;
        List<Player> topTotalScoreUuid = playerRepository.findTopTotalScoreUuid(PageRequest.of(0, limit));

        List<Ranking> rankingList = new ArrayList<>();
        int rank = 1;
        for (Player player : topTotalScoreUuid) {
            rankingList.add(Ranking.builder()
                    .rank(rank++)
                    .totalScore(player.getTotalScore())
                    .uuid(player.getUuid().substring(0, 8))
                    .build());
        }
        return rankingList;
    }
}
