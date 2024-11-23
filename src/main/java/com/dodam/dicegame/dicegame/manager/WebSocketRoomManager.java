package com.dodam.dicegame.dicegame.manager;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Getter
@Setter
@NoArgsConstructor
public class WebSocketRoomManager {

    private final Map<String, Set<String>> roomUserMap = new ConcurrentHashMap<>();

    public void addUserToRoom(String roomId, String userId) {
        roomUserMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        log.info("User {} added to room {}", userId, roomId);
    }

    public void removeUserFromRoom(String userId) {
        roomUserMap.forEach((roomId, users) -> {
            if (users.remove(userId)) {
                log.info("User {} removed from room {}", userId, roomId);
                if (users.isEmpty()) {
                    roomUserMap.remove(roomId);
                    log.info("Room {} is empty and removed", roomId);
                }
            }
        });
    }

    public Set<String> getUsersInRoom(String roomId) {
        return roomUserMap.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
    }
}
