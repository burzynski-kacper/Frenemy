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

    // Status effect flags for this specific fight
    private int playerAbsorbedDamage = 0; // Absorbed damage from dodges (Walkiria - 50% dodged damage)
    private int enemyAbsorbedDamage = 0; // Absorbed damage from enemy dodges (if enemy is Walkiria)
    private boolean playerIsFrozen = false;
    private boolean enemyIsFrozen = false;

    // Helper constructor to initialize only HP
    public CombatState(int playerHp, int enemyHp) {
        this.playerHp = playerHp;
        this.enemyHp = enemyHp;
    }
}