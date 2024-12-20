package com.dodam.dicegame.dicegame.repository;


import com.dodam.dicegame.dicegame.entity.Room;
import com.dodam.dicegame.dicegame.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByRoom(Room room);

    boolean existsByNickNameAndRoomId(String nickName, Long roomId);
}
