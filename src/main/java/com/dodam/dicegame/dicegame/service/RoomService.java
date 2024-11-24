package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.vo.RoomInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public Long createRoom(RoomInfoVO roomInfoVO) {
        Room saveRoom = roomRepository.save(Room.builder().roomName(roomInfoVO.getRoomName())
                .diceCount(roomInfoVO.getDiceCount())
                .maxPlayers(roomInfoVO.getMaxPlayers())
                .targetNumber(roomInfoVO.getTargetNumber())
                .entryCode(roomInfoVO.getEntryCode())
                .build());

        return saveRoom.getId();
    }

}
