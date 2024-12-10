package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.dodam.dicegame.dicegame.service.WebSocketRoomService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRoomService webSocketRoomService;
    private final List<BroadcastByActionType> broadcastByActionTypes;
    private final Gson gson;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message from {}: {}", session.getId(), message.getPayload());

        SocketPayloadVO socketPayloadVO = gson.fromJson(message.getPayload(), SocketPayloadVO.class);
        log.info("SocketPayloadVO : {}", socketPayloadVO);

        broadcastByActionTypes.stream()
                .filter(action -> action.isAction(socketPayloadVO.getAction()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("구현체를 찾을 수 없습니다."))
                .broadcastToClient(session, socketPayloadVO);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        webSocketRoomService.removeSessionById(session.getId());
    }

}
