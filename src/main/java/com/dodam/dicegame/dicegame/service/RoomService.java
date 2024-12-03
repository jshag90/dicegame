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
                .build());

        //방 생성자가 방장
        Player savePlayer = playerRepository.save(Player.builder().room(saveRoom)
                .isManager(RoomManager.MANAGER.getValue())
                .nickName(roomInfoVO.getNickName())
                .build());

        return saveRoom.getId();
    }

    public Long joinSecretRoomPlayer(JoinRoomPlayerVO joinRoomPlayerVO) throws TooManyPlayerException, NoExistRoomException, SameNickNamePlayerException {
        Room findRoom = roomRepository.findByIdAndEntryCode(joinRoomPlayerVO.getRoomId(), joinRoomPlayerVO.getEntryCode())
                .orElseThrow(() -> new NoExistRoomException("올바른 방 정보가 아님"));

        if (playerRepository.isNickNameDuplicate(findRoom, joinRoomPlayerVO.getNickName())) {
            throw new SameNickNamePlayerException("찾은방에 이미 동일한 닉네임의 사용자가 존재");
        }

        if (playerRepository.countByRoom(findRoom) >= findRoom.getMaxPlayers()) {
            throw new TooManyPlayerException("설정된 사용자 인원보다 많이 등록 할 수 없습니다.");
        }

        return findRoom.getId();
    }



    /**
     * 예외 정책에 통과하면 찾은 방번호 return
     * @param nickName
     * @return
     */
    public Long checkJoinPublicRoomPlayer(String nickName) throws NoExistRoomException, SameNickNamePlayerException {

        Room findPublicRoom = roomRepository.findAvailableMaxPlayerPublicRoom(RoomType.PUBLIC.getValue(), PageRequest.of(0, 1))
                                            .orElseThrow(() -> new NoExistRoomException("공개방이 존재하지 않습니다."));

        if (playerRepository.isNickNameDuplicate(findPublicRoom, nickName)) {
            throw new SameNickNamePlayerException("찾은방에 이미 동일한 닉네임의 사용자가 존재");
        }

        return findPublicRoom.getId();
    }

    public RoomPlayerInfo handleJoinRoomPlayer(Long roomId, String nickName) {

        Room findPublicRoom = roomRepository.findById(roomId).get();

        Player joinPlayer = playerRepository.save(Player.builder()
                .nickName(nickName)
                .room(findPublicRoom)
                .isManager(RoomManager.NORMAL.getValue())
                .build());

        return RoomPlayerInfo.builder().targetNumber(findPublicRoom.getTargetNumber())
                .diceCount(findPublicRoom.getDiceCount())
                .playerId(joinPlayer.getId())
                .build();
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
