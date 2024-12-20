package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

@RequiredArgsConstructor
@Component
@Slf4j
public class StartGameAction implements BroadcastByActionType {

    private final WebSocketRoomService webSocketRoomService;

    private final RoomRepository roomRepository;

    private final Gson gson;

    @Override
    public boolean isAction(String action) {
        return "startGame".equals(action);
    }

    @Override
    @Transactional
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {
        roomRepository.updateIsGameStarted(socketPayloadVO.getRoomId(), "Y");
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(socketPayloadVO.getRoomId() + "번방에서 게임을 시작합니다.")
                .build();
        broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));
    }

}
