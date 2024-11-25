package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.exception.TooManyPlayerException;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.util.RoomManager;
import com.dodam.dicegame.dicegame.vo.JoinRoomPlayerVO;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
import com.dodam.dicegame.dicegame.vo.RoomSettingInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final PlayerRepository playerRepository;

    public Long createRoom(RoomInfoVO roomInfoVO) {
        Room saveRoom = roomRepository.save(Room.builder().roomName(roomInfoVO.getRoomName())
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

    public Long joinRoomPlayer(JoinRoomPlayerVO joinRoomPlayerVO) throws TooManyPlayerException, NoExistRoomException {

        Optional<Room> joinRoom = roomRepository.findByIdAndEntryCode(joinRoomPlayerVO.getRoomId(), joinRoomPlayerVO.getEntryCode());

        if (!joinRoom.isPresent()) {
            throw new NoExistRoomException("올바른 방 정보가 아님");
        }

        Room findRoom = joinRoom.get();

        if (playerRepository.countByRoom(findRoom) >= findRoom.getMaxPlayers()) {
            throw new TooManyPlayerException("설정된 사용자 인원보다 많이 등록 할 수 없습니다.");
        }

        Player joinPlayer = playerRepository.save(Player.builder()
                .nickName(joinRoomPlayerVO.getNickName())
                .room(findRoom)
                .isManager(RoomManager.NORMAL.getValue())
                .build());

        return joinPlayer.getId();
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
                .roomName(getRoom.getRoomName())
                .targetNumber(getRoom.getTargetNumber())
                .build();
    }
}
