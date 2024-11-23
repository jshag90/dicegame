package com.dodam.dicegame.dicegame.sockethandler;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sessionMap.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message from {}: {}", session.getId(), message.getPayload());

        // 메시지 JSON 처리
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String action = payload.get("action");
        String roomId = payload.get("roomId");

        if ("joinRoom".equals(action)) {
            log.info("User {} joined room {}", session.getId(), roomId);
            broadcastToRoom(session, roomId, session.getId() + " joined the room.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        sessionMap.remove(session.getId());
    }

    private void broadcastToRoom(WebSocketSession senderSession, String roomId, String message) {
        sessionMap.values().forEach(session -> {
            try {
                if (!session.getId().equals(senderSession.getId())) {
                    session.sendMessage(new TextMessage("Room " + roomId + ": " + message));
                }
            } catch (Exception e) {
                log.error("Failed to send message", e);
            }
        });
    }
}
