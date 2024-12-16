package com.dodam.dicegame.dicegame.repository;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    long countByRoom(Room room);

    @Query("SELECT p.room FROM Player p WHERE p.id = :playerId")
    Room findRoomByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.room = :room AND p.nickName = :nickName")
    boolean isNickNameDuplicate(@Param("room") Room room, @Param("nickName") String nickName);

    @Query("UPDATE Player p SET p.nickName = :nickName WHERE p.id = :playerId")
    @Modifying
    void updateNickNameById(@Param("playerId") Long playerId, @Param("nickName") String nickName);

    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.id = :playerId AND p.nickName = :nickName")
    boolean existsByIdAndNickName(@Param("playerId") Long playerId, @Param("nickName") String nickName);
    @Query("SELECT p.id FROM Player p WHERE p.room.id = :roomId AND p.nickName = :nickName")
    Long findIdByRoomIdAndNickName(Long roomId, String nickName);


}
