package com.example.demo.repository;

import com.example.demo.model.Enemy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnemyRepository extends JpaRepository<Enemy, Long> {

    List<Enemy> findByEnemyType(String enemyType);

    List<Enemy> findByLevelBetween(int minLevel, int maxLevel);

    @Query("SELECT e FROM Enemy e WHERE e.level BETWEEN :playerLevel - 2 AND :playerLevel + 2")
    List<Enemy> findEnemiesAroundLevel(int playerLevel);
}