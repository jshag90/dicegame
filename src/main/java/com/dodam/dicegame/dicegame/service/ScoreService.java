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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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

    Sort totalScoreSort = Sort.by(Sort.Order.desc("totalScore"), Sort.Order.desc("id"));

    private final ReentrantLock lock = new ReentrantLock();
    @Transactional
    public void saveScore(SaveScoreVO saveScoreVO) {

        if (roomRepository.findById(saveScoreVO.getRoomId()).isPresent()) {

            Room findRoom = roomRepository.findById(saveScoreVO.getRoomId()).get();

            lock.lock();
            try {
                saveScoreByExistsScore(saveScoreVO, findRoom);
            } finally {
                lock.unlock();
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
    public List<ScoreResults> getGameScoreResults(Long roomId, String uuid) throws NoExistRoomException, InterruptedException {
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

        List<ScoreResults> zeroScoreList = scoreResultsList.stream().filter(scoreResults -> scoreResults.getScore().equals(0)).toList();
        boolean isAllPlayerZeroScore = zeroScoreList.size() == scoreResultsList.size();

        // 정렬 및 순위 설정
        DataUtil.sortScoreResults(scoreResultsList);

        // 해당 uuid 승점 증가
        int rank = 1;
        for (ScoreResults scoreResult : scoreResultsList) {
            int plusScore = rankToScoreMap.getOrDefault(rank, -1);
            plusScore = isAllPlayerZeroScore ? 0 : plusScore;

            scoreResult.setRank(rank);
            scoreResult.setPlusTotalScore(plusScore);

            if (uuid.equals(scoreResult.getUuid())) {
                playerRepository.incrementTotalScoreByUuid(uuid, plusScore);
            }

            rank++;
        }

        log.info("scoreResultsList() {}", scoreResultsList);
        return scoreResultsList;
    }

    public List<Ranking> getRanking() {

        int limit = 10;

        Pageable pageable = PageRequest.of(0, limit, totalScoreSort);
        List<Player> topTotalScoreUuid = playerRepository.findAll(pageable).getContent();
        List<Ranking> rankingList = new ArrayList<>();
        int rank = 1;
        for (Player player : topTotalScoreUuid) {
            rankingList.add(Ranking.builder()
                    .rank(rank++)
                    .totalScore(player.getTotalScore())
                    .uuid(player.getUuid())
                    .build());
        }
        log.info("rankingList : {}", rankingList);
        return rankingList;
    }

    public Ranking findPlayerRankingByUuid(String uuid) {
        int rank = 0;
        int totalScore = 0;

        int rankIndex = 1;
        for (Player player : playerRepository.findAll(totalScoreSort)) {
            if (player.getUuid().equals(uuid)) {
                rank = rankIndex;
                totalScore = player.getTotalScore();
                break;
            }
            rankIndex++;

        }
        Ranking ranking = Ranking.builder().rank(rank).totalScore(totalScore).uuid(uuid).build();
        log.info(ranking.toString());
        return ranking;
    }
}
