package com.dodam.dicegame.dicegame.repository;


import com.dodam.dicegame.dicegame.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
}
