package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.vo.JoinRoomPlayerVO;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
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
                .roomType(roomInfoVO.getRoomType())
                .entryCode(roomInfoVO.getEntryCode())
                .build());

        //방 생성자가 방장
        Player savePlayer = playerRepository.save(Player.builder().room(saveRoom)
                .isManager("Y")
                .nickName(roomInfoVO.getNickName())
                .build());

        return saveRoom.getId();
    }

    public Long joinRoomPlayer(JoinRoomPlayerVO joinRoomPlayerVO) {

        Optional<Room> joinRoom = roomRepository.findByIdAndEntryCode(joinRoomPlayerVO.getRoomId(), joinRoomPlayerVO.getEntryCode());

        Player joinPlayer = playerRepository.save(Player.builder()
                .nickName(joinRoomPlayerVO.getNickName())
                .room(joinRoom.get())
                .isManager("N")
                .build());

        return joinPlayer.getId();
    }
}
