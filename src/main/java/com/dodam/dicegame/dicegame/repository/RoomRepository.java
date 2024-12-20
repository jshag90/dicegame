package com.dodam.dicegame.dicegame.repository;

import com.dodam.dicegame.dicegame.entity.Room;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByIdAndEntryCode(Long roomId, String entryCode);

    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.maxPlayers > (SELECT COUNT(p) FROM Player p WHERE p.room.id = r.id )")
    Optional<Room> findAvailableMaxPlayerPublicRoom(@Param("roomType") String roomType, PageRequest pageable);

    @Modifying
    @Query("update Room r set r.isGameStarted = :isGameStarted where r.id = :roomId")
    void updateIsGameStarted(String roomId, String isGameStarted);
}
