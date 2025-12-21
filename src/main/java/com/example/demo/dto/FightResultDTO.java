package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FightResultDTO {
    private boolean playerWon;
    private String playerName;
    private String enemyName;
    private int playerHpStart;
    private int playerHpEnd;
    private int enemyHpStart;
    private int enemyHpEnd;
    private int turnsCount;
    private List<FightTurnDTO> fightLog;

    // Nagrody (tylko jeśli wygrał)
    private int xpGained;
    private int goldGained;
    private boolean leveledUp;
    private int newLevel;
}