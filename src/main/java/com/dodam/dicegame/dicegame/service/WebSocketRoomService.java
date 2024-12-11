package com.dodam.dicegame.dicegame.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketRoomService {

    public final Map<String, Set<String>> roomIdSessionIdMap = new ConcurrentHashMap<>(); //roomId, sessionId
    public final Map<String, Set<String>> roomIdPlayDoneMap = new ConcurrentHashMap<>(); //roomId, playDoneSessionId

    public final Map<String, Integer> roomIdStopCountMap = new ConcurrentHashMap<>();
    public final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>(); //sessionId, WebSocketSession

    public void addSessionToRoom(String roomId, String sessionId, WebSocketSession session) {
        log.info("addSessionToRoom() :  {},{},{}", roomId, sessionId, session);
        roomIdSessionIdMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionMap.put(sessionId, session);
    }

    public WebSocketSession getSessionById(String sessionId) {
        log.info("getSessionById() : {}", sessionId);
        return sessionMap.get(sessionId);
    }

    public Set<String> getSessionsInRoom(String roomId) {
        log.info("getSessionsInRoom() : {}", roomId);
        return roomIdSessionIdMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
    }

    public Integer getRoomMembersCount(String roomId) {
        log.info("getRoomMembersCount() : {}", roomId);
        return roomIdSessionIdMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet()).size();
    }

    public void removeSessionById(String sessionId) {
        log.info("removeSessionById() : {}", sessionId);
        sessionMap.remove(sessionId);
        roomIdSessionIdMap.values().forEach(sessions -> sessions.remove(sessionId));
    }

}
