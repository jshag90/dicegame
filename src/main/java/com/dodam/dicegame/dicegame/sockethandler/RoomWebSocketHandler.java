package com.dodam.dicegame.dicegame.sockethandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.dodam.dicegame.dicegame.manager.WebSocketRoomManager;

import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRoomManager roomManager; // 생성자 주입을 통해 WebSocketRoomManager 의존성 주입
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message from {}: {}", session.getId(), message.getPayload());

        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String action = payload.get("action");
        String roomId = payload.get("roomId");

        if ("joinRoom".equals(action)) {
            roomManager.addUserToRoom(roomId, session.getId());
            broadcastToRoom(roomId, session.getId() + " joined the room.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        roomManager.removeUserFromRoom(session.getId());
    }

    private void broadcastToRoom(String roomId, String message) {
        Set<String> users = roomManager.getUsersInRoom(roomId);
        if (users != null) {
            users.forEach(userId -> {
                try {
                    // 세션을 찾고, 메시지를 보냄
                    // WebSocketSession을 사용자 ID로 가져와 메시지를 보냄
                } catch (Exception e) {
                    log.error("Failed to send message to user {}", userId, e);
                }
            });
        }
    }
}
