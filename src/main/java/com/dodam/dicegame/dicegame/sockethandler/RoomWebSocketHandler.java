package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
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
    private final GetRoomsCountAction getRoomsCountAction;

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
    public void afterConnectionClosed(WebSocketSession closedSession, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed 연결이 종료됨: {}", closedSession.getId());
        //해당 방이 게임을 진행 중이면 broadcast

        String roomId = webSocketRoomService.getRoomIdBySessionId(closedSession.getId());
        String closedUuid = webSocketRoomService.getUuidBySessionId(closedSession.getId());
        log.info("closedUuid : {}", closedUuid);
        log.info("나간 방번호 : {}", roomId);

        Optional<Room> findRoom = roomRepository.findById(Long.valueOf(roomId));
        boolean isGameStartedRoom = false;
        if (findRoom.isPresent()) {
            isGameStartedRoom = findRoom.get().getIsGameStarted().equals("Y");
        }

        if (isGameStartedRoom) {
            webSocketRoomService.putRoomPlayDone(roomId, closedSession.getId());
            webSocketRoomService.putRoomIdStopCountMap(roomId, closedSession.getId());

            Set<String> roomAllSession = webSocketRoomService.getSessionsInRoom(roomId);
            //게임 진행 여부 메시지 전달
            broadCastExceptClosedSession(closedSession, roomId, roomAllSession, ResponseSocketPayloadVO.builder()
                    .action("playGame")
                    .message(webSocketRoomService.getPlayDoneMessage(roomId, true))
                    .build());

            webSocketRoomService.removeSessionById(closedSession.getId());

            SocketPayloadVO socketPayloadVO = SocketPayloadVO.builder().action("getRoomsCount").roomId(roomId).build();
            getRoomsCountAction.broadcastToClient(closedSession, socketPayloadVO);

            allSaveScoreLatchMap.putIfAbsent(Long.valueOf(roomId), new CountDownLatch(1));
            allSaveScoreLatchMap.get(Long.valueOf(roomId)).countDown();


            //퇴장 메시지 전달
            broadCastExceptClosedSession(closedSession, roomId, roomAllSession, ResponseSocketPayloadVO.builder()
                    .action("leaveRoom")
                    .message(closedUuid.substring(1, 9))
                    .subMessage("ok")
                    .build());

        }

        //해당 방이 게임 시작 전이면 접속 인원 갱신


    }


    private void broadCastExceptClosedSession(WebSocketSession closedSession, String roomId, Set<String> roomAllSession, ResponseSocketPayloadVO responseSocketPayloadVO) {
        roomAllSession.forEach(sessionId -> {
            if (sessionId.equals(closedSession.getId())) {
                return;
            }

            WebSocketSession session = webSocketRoomService.getSessionById(sessionId);
            try {
                session.sendMessage(new TextMessage(gson.toJson(responseSocketPayloadVO)));
            } catch (IOException e) {
                log.info("Failed to send message to session " + sessionId + " in room " + roomId);
                e.printStackTrace();
            }
        });
    }


}
