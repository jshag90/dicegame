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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    private final PlayerRepository playerRepository;

    public Long createRoom(RoomInfoVO roomInfoVO) {
        Room saveRoom = roomRepository.save(Room.builder()
                .diceCount(roomInfoVO.getDiceCount())
                .maxPlayers(roomInfoVO.getMaxPlayers())
                .targetNumber(roomInfoVO.getTargetNumber())
                .roomType(roomInfoVO.getRoomType().getValue())
                .entryCode(roomInfoVO.getEntryCode())
                .isGameStarted("N")
                .build());

        //방 생성자가 방장
        Player savePlayer = playerRepository.save(Player.builder().room(saveRoom)
                .isManager(RoomManager.MANAGER.getValue())
                .nickName(roomInfoVO.getNickName())
                .build());

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
     */
    public Room checkJoinPublicRoomPlayer() throws NoExistRoomException {
        return roomRepository.findAvailableMaxPlayerPublicRoom(RoomType.PUBLIC.getValue(), PageRequest.of(0, 1))
                .orElseThrow(() -> new NoExistRoomException("공개방이 존재하지 않습니다."));
    }

    public boolean isAlreadyUsedNickName(Room findPublicRoom, String nickName) {
        return playerRepository.isNickNameDuplicate(findPublicRoom, nickName);
    }


    public RoomPlayerInfo handleJoinRoomPlayer(Long roomId, String nickName) {

        Room findRoom = roomRepository.findById(roomId).get();

        Player joinPlayer = playerRepository.save(Player.builder()
                .nickName(nickName)
                .room(findRoom)
                .isManager(playerRepository.existsByRoomIdAndIsManager(roomId)?RoomManager.NORMAL.getValue():RoomManager.MANAGER.getValue())
                .build());

        RoomPlayerInfo roomPlayerInfo = RoomPlayerInfo.builder().targetNumber(findRoom.getTargetNumber())
                .diceCount(findRoom.getDiceCount())
                .playerId(joinPlayer.getId())
                .isRoomMaster(joinPlayer.getIsManager())
                .nickName(joinPlayer.getNickName())
                .roomId(findRoom.getId())
                .maxPlayer(findRoom.getMaxPlayers())
                .entryCode(findRoom.getEntryCode().isBlank()?"-1":findRoom.getEntryCode())
                .isPublic(findRoom.getRoomType().equals("secret")?"false":"true")
                .build();
        log.info(roomPlayerInfo.toString());
        return roomPlayerInfo;
    }

    /**
     * 해당 번호의 방번호가 있으면 삭제
     * @param roomId
     */
    public void removeRoom(Long roomId) {
        Optional<Room> findRoom = roomRepository.findById(roomId);
        findRoom.ifPresent(roomRepository::delete);
    }

    public RoomSettingInfoVO getRoomSettingInfo(Long roomId) throws NoExistRoomException {
        Optional<Room> findRoom = roomRepository.findById(roomId);

        if (!findRoom.isPresent()) {
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
