package com.dodam.dicegame.dicegame.repository;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    long countByRoom(Room room);

    @Modifying
    @Query("UPDATE Player p SET p.room = NULL, p.isManager = 'N' WHERE p.id = :playerId")
    void updateRoomIdNullByPlayerId(@Param("playerId") Long playerId);

    @Modifying
    @Query("UPDATE Player p SET p.isManager = :isManager WHERE p.id = :playerId")
    void updateIsMasterById(@Param("playerId") Long playerId, @Param("isManager") String isManager);

    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.uuid = :uuid")
    boolean existsByUuid(@Param("uuid") String uuid);

    @Query("SELECT p FROM Player p WHERE p.uuid = :uuid")
    Player findPlayerByUuid(@Param("uuid") String uuid);

    @Query("UPDATE Player p SET p.isManager = :isManager WHERE p.uuid = :uuid")
    @Modifying
    void updateIsManager(@Param("uuid") String uuid, @Param("isManager") String isManager);

    @Query("UPDATE Player p SET p.room.id = :roomId WHERE p.uuid = :uuid")
    @Modifying
    void updateRoomId(@Param("uuid") String uuid, @Param("roomId") Long roomId);

    @Query("SELECT p.id FROM Player p WHERE p.room.id = :roomId AND p.uuid = :uuid")
    Long findIdByRoomIdAndUuid(Long roomId, String uuid);

    @Query("SELECT p.id FROM Player p WHERE p.room.id = :roomId AND p.uuid != :uuid")
    List<Long> findIdByRoomIdAndNotUuid(Long roomId, String uuid);

    /**
     * 방장이 있는 지 검사
     * @param roomId
     * @return
     */
    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.room.id = :roomId AND p.isManager = 'Y'")
    boolean existsByRoomIdAndIsManager(@Param("roomId") Long roomId);


    @Query("UPDATE Player p SET p.totalScore = p.totalScore + :plusScore WHERE p.uuid = :uuid")
    @Modifying
    void incrementTotalScoreByUuid(@Param("uuid") String uuid, @Param("plusScore") int plusScore);

    Optional<Player> findByUuid(String uuid);

}
