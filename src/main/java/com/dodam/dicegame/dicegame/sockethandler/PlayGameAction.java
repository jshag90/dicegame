package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.repository.RoomRepository;
import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    private final RoomRepository roomRepository;

    @Override
    public boolean isAction(String action) {
        return "playGame".equals(action);
    }

    @Override
    @Transactional
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {
        webSocketRoomService.putRoomPlayDone(socketPayloadVO.getRoomId(), session.getId());

        //플레이어가 stop을 선택했을 경우
        if (socketPayloadVO.getIsGo().equals("N")) {
            webSocketRoomService.putRoomIdStopCountMap(socketPayloadVO.getRoomId(), session.getId());
        }

        String playDoneMessage = webSocketRoomService.getPlayDoneMessage(socketPayloadVO.getRoomId(),false);

        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(playDoneMessage)
                .build();

        log.info("broadcastToRoom() {}", gson.toJson(responseSocketPayloadVO));
        broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));

        if ("done".equals(playDoneMessage)) {
            webSocketRoomService.roomIdPlayDoneMap.remove(socketPayloadVO.getRoomId());
        }

        roomRepository.updateUpdatedAt(Long.valueOf(socketPayloadVO.getRoomId()));
    }


}
