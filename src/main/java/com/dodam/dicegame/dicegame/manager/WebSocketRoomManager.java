package com.dodam.dicegame.dicegame.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketRoomManager {

    private final Map<String, Set<String>> roomUserMap = new ConcurrentHashMap<>();

    public void addUserToRoom(String roomId, String userId) {
        roomUserMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        log.info("User {} added to room {}", userId, roomId);
    }

    public void removeUserFromRoom(String roomId, String userId) {
        Set<String> users = roomUserMap.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                roomUserMap.remove(roomId);
            }
        }
        log.info("User {} removed from room {}", userId, roomId);
    }

    public Set<String> getUsersInRoom(String roomId) {
        return roomUserMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
    }
}
