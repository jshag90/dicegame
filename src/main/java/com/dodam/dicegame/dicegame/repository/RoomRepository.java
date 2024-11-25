package com.dodam.dicegame.dicegame.repository;

import com.dodam.dicegame.dicegame.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByIdAndEntryCode(Long roomId, String entryCode);
}
