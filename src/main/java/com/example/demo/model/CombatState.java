package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CombatState {
    private int playerHp;
    private int enemyHp;

    // Flagi efektów statusu dla tej konkretnej walki
    private boolean playerHasDiveBonus = false;
    private boolean enemyHasDiveBonus = false;
    private boolean playerIsFrozen = false;
    private boolean enemyIsFrozen = false;

    // Konstruktor pomocniczy do inicjalizacji samym HP
    public CombatState(int playerHp, int enemyHp) {
        this.playerHp = playerHp;
        this.enemyHp = enemyHp;
    }
}