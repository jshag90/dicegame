package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
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

            if(!session.isOpen()){
                return;
            }

            try {
                session.sendMessage(new TextMessage(message));
            } catch (IllegalStateException e) {
                // 세션이 닫혀 메시지 전송이 실패한 경우
                log.info("Failed to send message because session " + sessionId + " is closed for room " + roomId);
            } catch (IOException e) {
                // 기타 메시지 전송 실패 처리
                log.info("Failed to send message to session " + sessionId + " in room " + roomId);
            }
        });
    }

    default boolean isSkipSession(WebSocketSession session, String selfSessionId, String sessionId) {
        return session == null || !session.isOpen() || selfSessionId.equals(sessionId);
    }


}
