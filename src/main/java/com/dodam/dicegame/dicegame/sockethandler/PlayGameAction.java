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
            putRoomIdStopCountMap(socketPayloadVO);
        }

        int roomStopCount = webSocketRoomService.roomIdStopCountMap.get(socketPayloadVO.getRoomId()) == null ? 0 : webSocketRoomService.roomIdStopCountMap.get(socketPayloadVO.getRoomId());

        int roomAllSessionSize = roomAllSession.size() - roomStopCount;
        String playDoneMessage = roomAllSessionSize == roomPlayDoneSession.size() ? "done" : "wait";
        playDoneMessage = isAllPlayerStop(roomAllSession, roomStopCount, playDoneMessage); //모든 플레이어가 stop을 선택했을 경우
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(playDoneMessage)
                .build();

        broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));

        if ("done".equals(playDoneMessage)) {
            webSocketRoomService.roomIdPlayDoneMap.remove(socketPayloadVO.getRoomId());
        }
    }

    private static String isAllPlayerStop(Set<String> roomAllSession, int roomStopCount, String playDoneMessage) {
        return roomAllSession.size() == roomStopCount ? "end" : playDoneMessage;
    }

    private void putRoomIdStopCountMap(SocketPayloadVO socketPayloadVO) {
        if (webSocketRoomService.roomIdStopCountMap.get(socketPayloadVO.getRoomId()) == null) {
            webSocketRoomService.roomIdStopCountMap.put(socketPayloadVO.getRoomId(), 1);
        } else {
            Integer currentStopCount = webSocketRoomService.roomIdStopCountMap.get(socketPayloadVO.getRoomId());
            webSocketRoomService.roomIdStopCountMap.put(socketPayloadVO.getRoomId(), currentStopCount + 1);
        }
    }

}
