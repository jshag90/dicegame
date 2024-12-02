package com.dodam.dicegame.dicegame.repository;

import com.dodam.dicegame.dicegame.entity.Player;
import com.dodam.dicegame.dicegame.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    long countByRoom(Room room);

    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.room = :room AND p.nickName = :nickName")
    boolean isNickNameDuplicate(@Param("room") Room room, @Param("nickName") String nickName);
}
