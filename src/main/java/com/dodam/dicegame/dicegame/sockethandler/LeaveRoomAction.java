package com.dodam.dicegame.dicegame.sockethandler;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.repository.PlayerRepository;
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

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class LeaveRoomAction implements BroadcastByActionType{

    private final WebSocketRoomService webSocketRoomService;

    private final Gson gson;

    private final PlayerRepository playerRepository;

    private final RoomRepository roomRepository;

    private final GetRoomsCountAction getRoomsCountAction;

    @Override
    public boolean isAction(String action) {
        return "leaveRoom".equals(action);
    }

    @Override
    @Transactional
    public void broadcastToClient(WebSocketSession session, SocketPayloadVO socketPayloadVO) {
        Long findPlayerId = playerRepository.findIdByRoomIdAndNickName(Long.valueOf(socketPayloadVO.getRoomId()), socketPayloadVO.getNickName());
        Player findPlayer = playerRepository.findById(findPlayerId).get();
        String message="",subMessage="";
        List<Long> otherNickNamePlayerIds = playerRepository.findIdByRoomIdAndNotNickName(Long.valueOf(socketPayloadVO.getRoomId()), socketPayloadVO.getNickName());

        webSocketRoomService.removeSessionById(session.getId());

        Room findRoom = roomRepository.findById(Long.valueOf(socketPayloadVO.getRoomId())).get();
        if(findRoom.getIsGameStarted().equals("N") && otherNickNamePlayerIds.size() > 0){

            if(findPlayer.getIsManager().equals("N")){
                message = socketPayloadVO.getNickName();
                subMessage = "ok";
            }

            if(findPlayer.getIsManager().equals("Y")){ //퇴장한 플레이어가 방장일 경우
                Long otherNickNamePlayerId = otherNickNamePlayerIds.get(0);
                Player findOtherPlayer = playerRepository.findById(otherNickNamePlayerId).get();
                playerRepository.updateIsMasterById(otherNickNamePlayerId, "Y");
                message = findOtherPlayer.getNickName();
                subMessage = "changeRoomMaster";
            }

            ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                    .action(socketPayloadVO.getAction())
                    .message(message)
                    .subMessage(subMessage)
                    .build();

            broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), null, gson.toJson(responseSocketPayloadVO));
            log.info("broadcastToRoom()-{}", responseSocketPayloadVO);
        }


        if(findRoom.getIsGameStarted().equals("Y") && otherNickNamePlayerIds.size() < 2){
            ResponseSocketPayloadVO responseSocketPayloadVO = ResponseSocketPayloadVO.builder()
                    .action("playGame")
                    .message("end")
                    .build();

            log.info("broadcastToRoom() {}", gson.toJson(responseSocketPayloadVO));
            broadcastToRoom(webSocketRoomService, socketPayloadVO.getRoomId(), session.getId(), gson.toJson(responseSocketPayloadVO));
        }

        getRoomsCountAction.broadcastToClient(session, SocketPayloadVO.builder().action("getRoomsCount").roomId(socketPayloadVO.getRoomId()).build());

    }

}
