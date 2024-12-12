package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
@Slf4j
public class PlayGameAction implements BroadcastByActionType {

    private final WebSocketRoomService webSocketRoomService;

    private final Gson gson;

    @Override
    public boolean isAction(String action) {
        return "playGame".equals(action);
    }

    @Override
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {
        webSocketRoomService.roomIdPlayDoneMap.computeIfAbsent(socketPayloadVO.getRoomId(), k -> ConcurrentHashMap.newKeySet()).add(session.getId());

        Set<String> roomAllSession = Optional.ofNullable(webSocketRoomService.roomIdSessionIdMap.get(socketPayloadVO.getRoomId())).orElse(Collections.emptySet());
        Set<String> roomPlayDoneSession = Optional.ofNullable(webSocketRoomService.roomIdPlayDoneMap.get(socketPayloadVO.getRoomId())).orElse(Collections.emptySet());

        //플레이어가 stop을 선택했을 경우
        if (socketPayloadVO.getIsGo().equals("N")) {
            putRoomIdStopCountMap(socketPayloadVO, session );
        }

        int roomStopCount = webSocketRoomService.roomIdStopSessionIdMap.get(socketPayloadVO.getRoomId()) == null ? 0 : webSocketRoomService.roomIdStopSessionIdMap.get(socketPayloadVO.getRoomId()).size();

        int roomAllSessionSize = roomAllSession.size() - roomStopCount;
        log.info("roomAllSessionSize {}", roomAllSessionSize);
        log.info("roomStopCount {}", roomStopCount);
        String playDoneMessage = roomAllSessionSize <= roomPlayDoneSession.size() ? "done" : "wait";
        playDoneMessage = roomAllSession.size() <= roomStopCount ? "end" : playDoneMessage;

        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(playDoneMessage)
                .build();

        log.info("broadcastToRoom() {}", gson.toJson(responseSocketPayloadVO));
        broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));

        if ("done".equals(playDoneMessage)) {
            webSocketRoomService.roomIdPlayDoneMap.remove(socketPayloadVO.getRoomId());
        }
    }


    private void putRoomIdStopCountMap(SocketPayloadVO socketPayloadVO, WebSocketSession session) {
        webSocketRoomService.roomIdStopSessionIdMap
                .computeIfAbsent(socketPayloadVO.getRoomId(), key -> ConcurrentHashMap.newKeySet())
                .add(session.getId());
        log.info("putRoomIdStopCountMap() {}", webSocketRoomService.roomIdStopSessionIdMap);
    }

}
