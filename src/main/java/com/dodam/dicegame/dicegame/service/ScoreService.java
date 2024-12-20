package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.dto.ScoreResults;
import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.entity.Score;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.repository.ScoreRepository;
import com.dodam.dicegame.dicegame.vo.SaveScoreVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final RoomRepository roomRepository;
    private final ScoreRepository scoreRepository;
    private final PlayerRepository playerRepository;
    private final Map<Long, CountDownLatch> latchMap = new ConcurrentHashMap<>();
    private final AtomicBoolean isSaving = new AtomicBoolean(false);

    public void saveScore(SaveScoreVO saveScoreVO) {

        Room findRoom = roomRepository.findById(saveScoreVO.getRoomId()).get();
        if (isSaving.compareAndSet(false, true)) {
            if (!scoreRepository.existsByNickNameAndRoomId(saveScoreVO.getNickName(),findRoom.getId())) {
                scoreRepository.save(
                        Score.builder()
                                .score(saveScoreVO.getScore())
                                .room(findRoom)
                                .nickName(saveScoreVO.getNickName())
                                .finalRound(saveScoreVO.getFinalRound())
                                .build()
                );
            }
            isSaving.set(false);
        }

        latchMap.putIfAbsent(saveScoreVO.getRoomId(), new CountDownLatch(1));

        if (playerRepository.countByRoom(findRoom) <= scoreRepository.findByRoom(findRoom).size()) {
            latchMap.get(saveScoreVO.getRoomId()).countDown();
        }

    }

    public List<ScoreResults> getGameScoreResults(Long roomId) throws NoExistRoomException, InterruptedException {
        Optional<Room> findRoom = roomRepository.findById(roomId);
        if (findRoom.isEmpty()) {
            throw new NoExistRoomException("해당 roomId가 존재하지 않음 : " + roomId);
        }

        latchMap.get(roomId).await();
        List<Score> findScore = scoreRepository.findByRoom(findRoom.get());

        int targetNumber = findRoom.get().getTargetNumber();
        List<ScoreResults> scoreResultsList = new ArrayList<>();
        for (Score score : findScore) {
            scoreResultsList.add(ScoreResults.builder()
                    .nickName(score.getNickName())
                    .score(score.getScore())
                    .roomId(roomId.intValue())
                    .targetNumber(targetNumber)
                    .build());
        }

        // 정렬 및 순위 설정
        sortScoreResults(scoreResultsList);
        assignRanks(scoreResultsList);
        log.info("scoreResultsList() {}", scoreResultsList);
        return scoreResultsList;
    }

    /**
     * ScoreResults를 targetNumber와 score 차이에 따라 정렬합니다.
     */
    private void sortScoreResults(List<ScoreResults> scoreResultsList) {
        scoreResultsList.sort((result1, result2) -> {
            int diff1 = result1.getTargetNumber() - result1.getScore();
            int diff2 = result2.getTargetNumber() - result2.getScore();

            // 양수와 음수 우선 비교
            if (diff1 >= 0 && diff2 < 0) {
                return -1; // diff1이 양수이고 diff2가 음수면 result1이 더 앞
            } else if (diff1 < 0 && diff2 >= 0) {
                return 1; // diff1이 음수이고 diff2가 양수면 result2가 더 앞
            }

            // 음수끼리 비교 (절대값 기준으로 정렬)
            if (diff1 < 0 && diff2 < 0) {
                return Integer.compare(Math.abs(diff1), Math.abs(diff2));
            }

            // 양수끼리 비교 (절대값 기준으로 정렬)
            return Integer.compare(Math.abs(diff1), Math.abs(diff2));
        });
    }

    /**
     * ScoreResults에 순위를 할당합니다.
     */
    private void assignRanks(List<ScoreResults> scoreResultsList) {
        for (int i = 0; i < scoreResultsList.size(); i++) {
            scoreResultsList.get(i).setRank(i + 1); // 1부터 시작하는 순위 설정
        }
    }


}
