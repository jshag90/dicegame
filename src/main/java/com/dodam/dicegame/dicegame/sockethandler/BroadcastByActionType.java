package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

public interface BroadcastByActionType {

    Logger log = LoggerFactory.getLogger(BroadcastByActionType.class);

    boolean isAction(String action);

    void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO);

    default void broadcastToRoom(
            WebSocketRoomService webSocketRoomService,
            String roomId,
            String selfSessionId,
            String message) {

        Set<String> sessionIds = webSocketRoomService.getSessionsInRoom(roomId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            log.info("No sessions found for room " + roomId);
            return;
        }

        sessionIds.forEach(sessionId -> {
            WebSocketSession session = webSocketRoomService.getSessionById(sessionId);
            if (session == null) {
                log.info("Session " + sessionId + " not found for room " + roomId);
                return;
            }

            if (selfSessionId != null && isSkipSession(session, selfSessionId, sessionId)) {
                return;
            }

            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.info("Failed to send message to session " + sessionId + " in room " + roomId);
                e.printStackTrace();
            }
        });
    }

    default boolean isSkipSession(WebSocketSession session, String selfSessionId, String sessionId) {
        return session == null || !session.isOpen() || selfSessionId.equals(sessionId);
    }


}
