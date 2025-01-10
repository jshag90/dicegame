package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.service.PlayerService;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.dodam.dicegame.dicegame.service.WebSocketRoomService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.dodam.dicegame.dicegame.service.ScoreService.allSaveScoreLatchMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRoomService webSocketRoomService;
    private final List<BroadcastByActionType> broadcastByActionTypes;
    private final Gson gson;
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final GetRoomsCountAction getRoomsCountAction;
    private final PlayerService playerService;

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
    @Transactional
    public void afterConnectionClosed(WebSocketSession closedSession, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed 연결이 종료됨: {}", closedSession.getId());

        String roomId = webSocketRoomService.getRoomIdBySessionId(closedSession.getId());
        log.info("나간 방번호 : {}", roomId);
        String closedUuid = webSocketRoomService.getUuidBySessionId(closedSession.getId());
        log.info("closedUuid : {}", closedUuid);

        Optional<Room> findRoom = roomRepository.findById(Long.valueOf(roomId));
        boolean isGameStartedRoom = findRoom.map(room -> room.getIsGameStarted().equals("Y")).orElse(false);

        Set<String> roomAllSession = webSocketRoomService.getSessionsInRoom(roomId);
        if (isGameStartedRoom) {
            webSocketRoomService.putRoomPlayDone(roomId, closedSession.getId());
            webSocketRoomService.putRoomIdStopCountMap(roomId, closedSession.getId());

            //게임 진행 여부 메시지 전달
            broadCastExceptClosedSession(closedSession, roomId, roomAllSession, getPlayGamePayloadVO(roomId));
            allSaveScoreLatchMap.putIfAbsent(Long.valueOf(roomId), new CountDownLatch(1));
            allSaveScoreLatchMap.get(Long.valueOf(roomId)).countDown();
        }

        String message = closedUuid.substring(1, 9), subMessage = "ok";
        if (!isGameStartedRoom) {
            List<Long> otherNickNamePlayerIds = playerRepository.findIdByRoomIdAndNotUuid(Long.valueOf(roomId), closedUuid);
            if (otherNickNamePlayerIds.size() > 0) {
                Optional<Player> playerOptional = playerRepository.findByUuid(closedUuid);
                if (playerOptional.isEmpty()) {
                    log.info("DB에 존재하지 않음");
                    return;
                }

                if (playerOptional.get().getIsManager().equals("Y")) {
                    Long otherNickNamePlayerId = otherNickNamePlayerIds.get(0);
                    playerRepository.updateIsMasterById(otherNickNamePlayerId, "Y");
                    Player findOtherPlayer = playerRepository.findById(otherNickNamePlayerId).get();
                    message = findOtherPlayer.getUuid();
                    subMessage = "changeRoomMaster";
                }
            }
        }

        broadCastExceptClosedSession(closedSession, roomId, roomAllSession, getLeaveRoomPayloadVO(message, subMessage));

        webSocketRoomService.removeSessionById(closedSession.getId());
        getRoomsCountAction.broadcastToClient(closedSession, getRoomsCountPayloadVO(roomId));

        playerService.deletePlayerInRoom(Long.valueOf(roomId), closedUuid);

    }



    private ResponseSocketPayloadVO getPlayGamePayloadVO(String roomId) {
        return ResponseSocketPayloadVO.builder()
                .action("playGame")
                .message(webSocketRoomService.getPlayDoneMessage(roomId, true))
                .build();
    }

    private ResponseSocketPayloadVO getLeaveRoomPayloadVO(String message, String subMessage) {
        return ResponseSocketPayloadVO.builder()
                .action("leaveRoom")
                .message(message)
                .subMessage(subMessage)
                .build();
    }

    private SocketPayloadVO getRoomsCountPayloadVO(String roomId) {
        return SocketPayloadVO.builder().action("getRoomsCount").roomId(roomId).build();
    }

    private void broadCastExceptClosedSession(WebSocketSession closedSession, String roomId, Set<String> roomAllSession, ResponseSocketPayloadVO responseSocketPayloadVO) {
        roomAllSession.forEach(sessionId -> {
            if (sessionId.equals(closedSession.getId())) {
                return;
            }

            WebSocketSession session = webSocketRoomService.getSessionById(sessionId);
            if (!session.isOpen()) {
                return;
            }
            try {
                session.sendMessage(new TextMessage(gson.toJson(responseSocketPayloadVO)));
            } catch (IOException e) {
                log.info("Failed to send message to session " + sessionId + " in room " + roomId);
                e.printStackTrace();
            }
        });
    }

}
