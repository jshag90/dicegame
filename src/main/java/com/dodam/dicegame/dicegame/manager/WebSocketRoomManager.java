package com.dodam.dicegame.dicegame.manager;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Getter
@Setter
@NoArgsConstructor
public class WebSocketRoomManager {

    public final Map<String, Set<String>> roomSessionIdMap = new ConcurrentHashMap<>(); //roomId,sessionId
    public final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>(); //sessionId,WebSocketSession

    public void addSessionToRoom(String roomId, String sessionId, WebSocketSession session) {
        roomSessionIdMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionMap.put(sessionId, session);
        log.info("addSessionToRoom roomSessionIdMap :  {}", roomSessionIdMap);
        log.info("addSessionToRoom sessionMap : {}", sessionMap);
    }

    public WebSocketSession getSessionById(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public void removeSessionById(String sessionId) {
        sessionMap.remove(sessionId);
        roomSessionIdMap.values().forEach(sessions -> sessions.remove(sessionId));
    }


    public Set<String> getSessionsInRoom(String roomId) {
        return roomSessionIdMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
    }

    public Integer getRoomMembersCount(String roomId) {
        Set<String> sessionIdList = roomSessionIdMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
        return sessionIdList.size();
    }


}
