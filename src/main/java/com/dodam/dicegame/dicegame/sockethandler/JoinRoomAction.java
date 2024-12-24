package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class JoinRoomAction implements BroadcastByActionType{

    private final WebSocketRoomService webSocketRoomService;

    private final Gson gson;

    private final PlayerRepository playerRepository;

    @Override
    public boolean isAction(String action) {
        return "joinRoom".equals(action);
    }

    @Override
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {
        webSocketRoomService.addSessionToRoom(socketPayloadVO.getRoomId(), session.getId(), session);
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(socketPayloadVO.getNickName() + "님이 입장하였습니다.")
                .build();

        Optional<Boolean> existNickNameRoomIdOptional = playerRepository.existsDuplicateNickNameInRoom(Long.valueOf(socketPayloadVO.getRoomId()));
        if (existNickNameRoomIdOptional.isPresent()) {
            if (!existNickNameRoomIdOptional.get()) {
                broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), session.getId(), gson.toJson(responseSocketPayloadVO));
            }
        } else {
            broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), session.getId(), gson.toJson(responseSocketPayloadVO));
        }
    }

}
