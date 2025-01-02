package com.dodam.dicegame.dicegame.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketRoomService {

    public final Map<String, Set<String>> roomIdSessionIdMap = new ConcurrentHashMap<>(); //roomId, sessionId
    public final Map<String, Set<String>> roomIdPlayDoneMap = new ConcurrentHashMap<>(); //roomId, playDoneSessionId
    public final Map<String, Set<String>> roomIdStopSessionIdMap = new ConcurrentHashMap<>(); //roomId, sessionId
    public final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>(); //sessionId, WebSocketSession
    public final Map<String, Set<String>> sessionIdUuidMap = new ConcurrentHashMap<>(); //sessionId, uuid

    public void addSessionToRoom(String roomId, String sessionId, String uuid, WebSocketSession session) {
        log.info("addSessionToRoom() :  {},{},{}", roomId, sessionId, session);
        roomIdSessionIdMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionIdUuidMap.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(uuid);
        sessionMap.put(sessionId, session);
    }

    public String getUuidBySessionId(String sessionId){
        return Optional.ofNullable(sessionIdUuidMap.get(sessionId)).orElse(Collections.emptySet()).toString();
    }

    public WebSocketSession getSessionById(String sessionId) {
        log.info("getSessionById() : {}", sessionId);
        return sessionMap.get(sessionId);
    }

    public Set<String> getSessionsInRoom(String roomId) {
        log.info("getSessionsInRoom() : {}", roomId);
        return Optional.ofNullable(roomIdSessionIdMap.get(roomId)).orElse(Collections.emptySet());
    }

    public Set<String> getPlayDoneSessionInRoom(String roomId) {
        return Optional.ofNullable(roomIdPlayDoneMap.get(roomId)).orElse(Collections.emptySet());
    }

    public String getRoomIdBySessionId(String sessionId) {
        log.info("getRoomIdBySessionId() {}", sessionId);
        return roomIdSessionIdMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(sessionId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }

    public Integer getRoomMembersCount(String roomId) {
        log.info("getRoomMembersCount() : {}", roomId);
        return roomIdSessionIdMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet()).size();
    }

    public void removeSessionById(String sessionId) {
        log.info("removeSessionById() : {}", sessionId);
        roomIdSessionIdMap.values().forEach(sessions -> sessions.remove(sessionId));
        roomIdStopSessionIdMap.values().forEach(sessions -> sessions.remove(sessionId));
        roomIdPlayDoneMap.values().forEach(sessions -> sessions.remove(sessionId));
        sessionMap.remove(sessionId);

        roomIdSessionIdMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        roomIdStopSessionIdMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        roomIdPlayDoneMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public int getRoomStopCount(String roomId){
        return roomIdStopSessionIdMap.get(roomId) == null ? 0 : roomIdStopSessionIdMap.get(roomId).size();
    }

    public void putRoomIdStopCountMap(String roomId, String sessionId) {
        roomIdStopSessionIdMap
                .computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        log.info("putRoomIdStopCountMap() {}", roomIdStopSessionIdMap);
    }

    public void putRoomPlayDone(String roomId, String sessionId){
        roomIdPlayDoneMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public String getPlayDoneMessage(String roomId, boolean isForceExit) {
        Set<String> roomAllSession = getSessionsInRoom(roomId);
        Set<String> roomPlayDoneSession = getPlayDoneSessionInRoom(roomId);

        int stopSessionCount = getRoomStopCount(roomId);
        int goSessionCount = roomAllSession.size() - stopSessionCount;

        // 모든 세션이 중지된 경우 또는 중지되지 않은 세션이 2명 미만인 경우
        if (roomAllSession.size() <= stopSessionCount || (isForceExit && goSessionCount < 2)) {
            return "end";
        }

        // 플레이를 완료한 세션이 중지되지 않은 세션과 같거나 많은 경우
        if (goSessionCount <= roomPlayDoneSession.size()) {
            return "done";
        }

        // 기본 메시지 반환
        return "wait";
    }

}
