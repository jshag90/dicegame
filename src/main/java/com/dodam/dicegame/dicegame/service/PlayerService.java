package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.exception.SameAlreadyNickNamePlayerException;
import com.dodam.dicegame.dicegame.exception.SameNickNamePlayerException;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (findPlayerId != null)
            playerRepository.updateRoomIdNullByPlayerId(findPlayerId);

        if(roomRepository.findById(roomId).isPresent()) {
            Room findRoom = roomRepository.findById(roomId).get();
            if (playerRepository.countByRoom(findRoom) == 0) {
                scoreRepository.deleteByRoom(findRoom);
                roomRepository.deleteById(roomId);
            }
        }
    }

    @Transactional
    public void updatePlayerNickName(Long playerId, String nickName) throws SameAlreadyNickNamePlayerException, SameNickNamePlayerException {

      /*  if (playerRepository.existsByIdAndNickName(playerId, nickName)) {
            throw new SameAlreadyNickNamePlayerException("동일한 닉네임으로 닉네임을 변경할 수 없습니다.");
        }*/

       /* Room plyerIdRoom = playerRepository.findRoomByPlayerId(playerId);
        if (playerRepository.isNickNameDuplicate(plyerIdRoom, nickName)) {
            throw new SameNickNamePlayerException("닉네임이 중복됩니다.");
        }*/

      /*  playerRepository.updateNickNameById(playerId, nickName);*/
    }


}
