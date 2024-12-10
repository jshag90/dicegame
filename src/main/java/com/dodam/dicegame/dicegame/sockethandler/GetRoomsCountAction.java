package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.service.WebSocketRoomService;
import com.dodam.dicegame.dicegame.vo.ResponseSocketPayloadVO;
import com.dodam.dicegame.dicegame.vo.SocketPayloadVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;


@RequiredArgsConstructor
@Component
@Slf4j
public class GetRoomsCountAction implements BroadcastByActionType{

    private final WebSocketRoomService webSocketRoomService;

    private final Gson gson;
    @Override
    public boolean isAction(String action) {
        return "getRoomsCount".equals(action);
    }

    @Override
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {

        Integer roomMembersCount = webSocketRoomService.getRoomMembersCount(socketPayloadVO.getRoomId());
        ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                .action(socketPayloadVO.getAction())
                .message(String.valueOf(roomMembersCount))
                .build();

        log.info("handleRoomMembersCountRequest() : " + responseSocketPayloadVO);
        broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));

    }

}
