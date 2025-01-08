package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.dto.RoomPlayerInfo;
import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.exception.SameNickNamePlayerException;
import com.dodam.dicegame.dicegame.exception.TooManyPlayerException;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.util.RoomManager;
import com.dodam.dicegame.dicegame.util.RoomType;
import com.dodam.dicegame.dicegame.vo.JoinRoomPlayerVO;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
import com.dodam.dicegame.dicegame.vo.RoomSettingInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    private final PlayerRepository playerRepository;

    @Transactional
    public Long createRoom(RoomInfoVO roomInfoVO) {
        Room saveRoom = roomRepository.save(Room.builder()
                .diceCount(roomInfoVO.getDiceCount())
                .maxPlayers(roomInfoVO.getMaxPlayers())
                .targetNumber(roomInfoVO.getTargetNumber())
                .roomType(roomInfoVO.getRoomType().getValue())
                .entryCode(roomInfoVO.getEntryCode())
                .isGameStarted("N")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        boolean isExistsUuid = playerRepository.existsByUuid(roomInfoVO.getUuid());
        if (isExistsUuid) {
            playerRepository.updateIsManager(roomInfoVO.getUuid(), RoomManager.MANAGER.getValue());
            playerRepository.updateRoomId(roomInfoVO.getUuid(), saveRoom.getId());
        }

        if (!isExistsUuid) {
            playerRepository.save(Player.builder().room(saveRoom)
                    .isManager(RoomManager.MANAGER.getValue())
                    .uuid(roomInfoVO.getUuid())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        return saveRoom.getId();
    }

    public Room checkSecretRoomPlayer(JoinRoomPlayerVO joinRoomPlayerVO) throws TooManyPlayerException, NoExistRoomException, SameNickNamePlayerException {
        Room findRoom = roomRepository.findByIdAndEntryCode(joinRoomPlayerVO.getRoomId(), joinRoomPlayerVO.getEntryCode())
                .orElseThrow(() -> new NoExistRoomException("올바른 방 정보가 아님"));

        if (playerRepository.countByRoom(findRoom) >= findRoom.getMaxPlayers()) {
            throw new TooManyPlayerException("설정된 사용자 인원보다 많이 등록 할 수 없습니다.");
        }

        return findRoom;
    }


    /**
     * 예외 정책에 통과하면 찾은 방번호 return
     * @return
     * @throws NoExistRoomException
     */
    public Room checkJoinPublicRoomPlayer() throws NoExistRoomException {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        return roomRepository.findAvailableMaxPlayerPublicRoom(RoomType.PUBLIC.getValue(), thirtyMinutesAgo, PageRequest.of(0, 1))
                .orElseThrow(() -> new NoExistRoomException("공개방이 존재하지 않습니다."));
    }


    @Transactional
    public RoomPlayerInfo handleJoinRoomPlayer(Long roomId, String uuid) {

        Room findRoom = roomRepository.findById(roomId).get();

        boolean isExistsUuid = playerRepository.existsByUuid(uuid);
        String isManager = playerRepository.existsByRoomIdAndIsManager(roomId) ? RoomManager.NORMAL.getValue() : RoomManager.MANAGER.getValue();
        Player joinPlayer = null;
        if (!isExistsUuid) {
            joinPlayer = playerRepository.save(Player.builder()
                                                     .uuid(uuid)
                                                     .room(findRoom)
                                                     .createdAt(LocalDateTime.now())
                                                     .isManager(isManager)
                                                     .build());
        }

        if (isExistsUuid) {
            playerRepository.updateIsManager(uuid, isManager);
            playerRepository.updateRoomId(uuid, roomId);
            joinPlayer = playerRepository.findPlayerByUuid(uuid);
        }

        RoomPlayerInfo roomPlayerInfo = RoomPlayerInfo.builder().targetNumber(findRoom.getTargetNumber())
                .diceCount(findRoom.getDiceCount())
                .playerId(joinPlayer.getId())
                .isRoomMaster(joinPlayer.getIsManager())
                .uuid(joinPlayer.getUuid())
                .roomId(findRoom.getId())
                .maxPlayer(findRoom.getMaxPlayers())
                .entryCode(findRoom.getEntryCode().isBlank() ? "-1" : findRoom.getEntryCode())
                .isPublic(findRoom.getRoomType().equals("secret") ? "false" : "true")
                .build();

        roomRepository.updateUpdatedAt(roomId);
        log.info(roomPlayerInfo.toString());
        return roomPlayerInfo;
    }

    /**
     * 해당 번호의 방번호가 있으면 삭제
     *
     * @param roomId
     */
    public void removeRoom(Long roomId) {
        Optional<Room> findRoom = roomRepository.findById(roomId);
        findRoom.ifPresent(roomRepository::delete);
    }

    public RoomSettingInfoVO getRoomSettingInfo(Long roomId) throws NoExistRoomException {
        Optional<Room> findRoom = roomRepository.findById(roomId);
        if (findRoom.isEmpty()) {
            throw new NoExistRoomException("올바른 방 정보가 아님");
        }

        Room getRoom = findRoom.get();
        return RoomSettingInfoVO.builder().roomType(getRoom.getRoomType())
                .diceCount(getRoom.getDiceCount())
                .maxPlayers(getRoom.getMaxPlayers())
                .targetNumber(getRoom.getTargetNumber())
                .build();
    }
}
