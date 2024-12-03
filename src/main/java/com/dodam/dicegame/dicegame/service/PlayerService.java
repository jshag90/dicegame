package com.dodam.dicegame.dicegame.service;

import com.dodam.dicegame.dicegame.dto.RoomPlayerInfo;
import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.exception.NoExistRoomException;
import com.dodam.dicegame.dicegame.exception.SameAlreadyNickNamePlayerException;
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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {


    private final PlayerRepository playerRepository;


    @Transactional
    public void updatePlayerNickName(Long playerId, String nickName) throws SameAlreadyNickNamePlayerException {

        if(playerRepository.existsByIdAndNickName(playerId, nickName)){
            throw new SameAlreadyNickNamePlayerException("동일한 닉네임으로 닉네임을 변경할 수 없습니다.");
        }

        playerRepository.updateNickNameById(playerId, nickName);
    }
}
