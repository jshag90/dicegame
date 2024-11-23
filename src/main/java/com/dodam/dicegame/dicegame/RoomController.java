package com.dodam.dicegame.dicegame;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestParam String name, @RequestParam int maxPlayers) {
        return (ResponseEntity<Room>) ResponseEntity.ok();
    }
}
