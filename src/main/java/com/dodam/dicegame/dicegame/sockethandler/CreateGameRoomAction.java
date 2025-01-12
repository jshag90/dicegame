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
public class CreateGameRoomAction implements BroadcastByActionType {

    private final WebSocketRoomService webSocketRoomService;

    private final Gson gson;

    @Override
    public boolean isAction(String action) {
        return "createGameRoom".equals(action);
    }

    @Override
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {
        webSocketRoomService.addSessionToRoom(socketPayloadVO.getRoomId(), session.getId(), socketPayloadVO.getUuid(), session);
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(socketPayloadVO.getRoomId() + "번방을 개설하였습니다.")
                .build();
        broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));
    }

}
