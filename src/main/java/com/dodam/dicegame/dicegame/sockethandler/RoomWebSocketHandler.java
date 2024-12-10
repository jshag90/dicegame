package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.dodam.dicegame.dicegame.manager.WebSocketRoomManager;

import java.io.IOException;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRoomManager roomManager;
    private final Gson gson = new Gson();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message from {}: {}", session.getId(), message.getPayload());
        SocketPayloadVO socketPayloadVO = gson.fromJson(message.getPayload(), SocketPayloadVO.class);
        log.info("SocketPayloadVO{}",socketPayloadVO);
        switch (socketPayloadVO.getAction()) {
            case "joinRoom" -> {
                roomManager.addSessionToRoom(socketPayloadVO.getRoomId(), session.getId(), session);
                ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                                                                                         .action(socketPayloadVO.getAction())
                                                                                         .message(socketPayloadVO.getNickName() + "님이 입장하였습니다.")
                                                                                         .build();
                broadcastToRoom(socketPayloadVO.getRoomId(), session.getId(), gson.toJson(responseSocketPayloadVO));
            }
            case "getRoomsCount" -> handleRoomMembersCountRequest(socketPayloadVO);
            case "startGame" ->{
                handleStartGameNotification(socketPayloadVO);
            }
            
            default -> log.warn("Unknown action: {}", socketPayloadVO.getAction());
        }



    }

    private void handleStartGameNotification(SocketPayloadVO socketPayloadVO) {
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(socketPayloadVO.getRoomId()+"번방 게임을 시작합니다.")
                .build();
        broadcastToRoom(socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));
    }

    private void handleRoomMembersCountRequest(SocketPayloadVO socketPayloadVO) throws IOException {
        Integer roomMembersCount = roomManager.getRoomMembersCount(socketPayloadVO.getRoomId());
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(String.valueOf(roomMembersCount))
                .build();

        log.info("handleRoomMembersCountRequest() : " + responseSocketPayloadVO);
        broadcastToRoom(socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        roomManager.removeSessionById(session.getId());
    }


    private void broadcastToRoom(String roomId, String selfSessionId, String message) {
        Set<String> sessionIds = roomManager.getSessionsInRoom(roomId); // 방에 있는 세션 목록 조회
        if (sessionIds == null) {
            log.warn("No sessions found for room {}", roomId);
            return;
        }

        sessionIds.forEach(sessionId -> {
            WebSocketSession session = roomManager.getSessionById(sessionId);
            if (selfSessionId != null && isSkipSession(session, selfSessionId, sessionId))
                return;

            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                throw new RuntimeException("Failed to send message", e);
            }
        });
    }

    private boolean isSkipSession(WebSocketSession session, String selfSessionId, String sessionId) {
        return session == null || !session.isOpen() || selfSessionId.equals(sessionId);
    }

}
