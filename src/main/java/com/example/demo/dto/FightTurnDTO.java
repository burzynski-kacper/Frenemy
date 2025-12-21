package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FightTurnDTO {
    private int turnNumber;
    private String attacker;
    private String action;      // ATTACK, CRITICAL, DODGE, BLOCK, FRENZY
    private int damage;
    private int targetHpAfter;
    private String description;
}