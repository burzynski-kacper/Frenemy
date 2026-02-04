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
    private int playerAbsorbedDamage = 0;  // Zaabsorbowane obrażenia z uników (Walkiria - 50% unikniętych dmg)
    private int enemyAbsorbedDamage = 0;   // Zaabsorbowane obrażenia wroga (jeśli wróg to Walkiria)
    private boolean playerIsFrozen = false;
    private boolean enemyIsFrozen = false;

    // Konstruktor pomocniczy do inicjalizacji samym HP
    public CombatState(int playerHp, int enemyHp) {
        this.playerHp = playerHp;
        this.enemyHp = enemyHp;
    }
}