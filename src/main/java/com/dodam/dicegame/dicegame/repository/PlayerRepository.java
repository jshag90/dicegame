package com.dodam.dicegame.dicegame.repository;

import com.dodam.dicegame.dicegame.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
