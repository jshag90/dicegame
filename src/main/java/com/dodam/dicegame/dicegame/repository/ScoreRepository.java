package com.dodam.dicegame.dicegame.repository;


import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByRoom(Room room);

    boolean existsByUuidAndRoomId(String uuid, Long roomId);

    void deleteByRoom(Room room);


    @Query("UPDATE Score s SET s.finalRound = :finalRound, s.score = :score " +
            "WHERE s.room.id = :roomId AND s.uuid = :uuid")
    @Modifying
    void updateScoreAndRound(@Param("roomId") Long roomId,
                            @Param("uuid") String uuid,
                            @Param("finalRound") Integer finalRound,
                            @Param("score") Integer score);

}
