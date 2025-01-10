package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.dto.PlayerInfo;
import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.exception.SameAlreadyNickNamePlayerException;
import com.dodam.dicegame.dicegame.exception.SameNickNamePlayerException;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.repository.ScoreRepository;
import com.dodam.dicegame.dicegame.util.RoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final ScoreRepository scoreRepository;

    @Transactional
    public void deletePlayerInRoom(Long roomId, String uuid) {
        Long findPlayerId = playerRepository.findIdByRoomIdAndUuid(roomId, uuid);
        if (findPlayerId != null) {
            playerRepository.updateRoomIdNullByPlayerId(findPlayerId);
        }

        if (roomRepository.findById(roomId).isPresent()) {
            Room findRoom = roomRepository.findById(roomId).get();
            if (playerRepository.countByRoom(findRoom) == 0) {
                scoreRepository.deleteByRoom(findRoom);
                roomRepository.deleteById(roomId);
            }
        }
    }

    public void savePlayerUuid(String uuid) {
        if (!playerRepository.existsByUuid(uuid)) {
            playerRepository.save(Player.builder()
                    .isManager(RoomManager.NORMAL.getValue())
                    .uuid(uuid)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    public PlayerInfo getPlayerInfoByUuid(String uuid){
        Optional<Player> player = playerRepository.findByUuid(uuid);
        if(player.isEmpty()){
            log.info("등록 되지 않은 uuid : {}", uuid);
            savePlayerUuid(uuid);
        }
        Long roomId = player.get().getRoom() == null ? -1L : player.get().getRoom().getId();
        return PlayerInfo.builder().uuid(player.get().getUuid())
                .createdAt(player.get().getCreatedAt())
                .isManager(player.get().getIsManager())
                .roomId(roomId)
                .totalScore(player.get().getTotalScore())
                .build();
    }

}
