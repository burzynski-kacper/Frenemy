package com.example.demo.util;

import com.example.demo.config.GameConfig;
import com.example.demo.model.Character;
import com.example.demo.model.Enemy;
import com.example.demo.model.Stats;

import java.util.Random;

public final class CombatCalculator {

    private static final Random random = new Random();

    private CombatCalculator() {}

    /**
     * Uniwersalna metoda do sprawdzania szansy procentowej (RNG).
     * Np. checkChance(25) zwróci true w 25% przypadków.
     */
    public static boolean checkChance(int percentage) {
        return random.nextInt(100) < percentage;
    }

    /**
     * Oblicza HP gracza.
     */
    public static int calculatePlayerHp(Character character) {
        Stats stats = character.getStats();
        // Base HP + HP z Wytrzymałości + HP z Levelu
        return (stats.getConstitution() * 5) + (character.getLevel() * 10) + (stats.getConstitution() * character.getLevel() / 2);
    }

    /**
     * Oblicza bazowe obrażenia gracza (zależne od klasy i statystyki głównej).
     */
    public static int calculatePlayerDamage(Character character) {
        Stats stats = character.getStats();
        String primaryStat = character.getCharacterClass().getPrimaryStat();

        int mainStatValue = switch (primaryStat) {
            case "Strength" -> stats.getStrength();
            case "Intelligence" -> stats.getIntelligence();
            case "Dexterity" -> stats.getDexterity();
            default -> stats.getStrength();
        };

        // Formuła dmg: (Stat * 1.5) + (Broń/Level base)
        int baseDamage = (int) (mainStatValue * 1.5) + (character.getLevel() * 2);

        return applyVariance(baseDamage);
    }

    /**
     * Oblicza obrażenia przeciwnika.
     */
    public static int calculateEnemyDamage(Enemy enemy) {
        // Przeciwnik bije ze swojej najwyższej statystyki
        int mainStat = Math.max(Math.max(enemy.getStrength(), enemy.getIntelligence()), enemy.getDexterity());
        int baseDamage = (int) (mainStat * 1.2) + (enemy.getLevel() * 2);

        return applyVariance(baseDamage);
    }

    /**
     * Dodaje losowość (wariancję) do obrażeń (±10%).
     */
    private static int applyVariance(int value) {
        int variance = Math.max(1, (int) (value * 0.1)); // 10% wariancji
        return value + random.nextInt(variance * 2 + 1) - variance;
    }

    /**
     * Sprawdza czy atak jest krytyczny.
     * Szansa = Szczęście * 2.5 / (Level przeciwnika).
     * Tutaj uproszczone do: Szczęście / 2 (max 50%).
     */
    public static boolean isCriticalHit(int luck) {
        int critChance = Math.min(50, luck / 2); // Cap na 50% szansy na krytyk
        return checkChance(critChance);
    }

    // === SPECYFICZNE MECHANIKI KLASOWE (Obliczenia wartości) ===

    /**
     * HUSKARL: Oblicza odbite obrażenia (Reflect).
     */
    public static int calculateReflectedDamage(int incomingDamage) {
        return (incomingDamage * GameConfig.HUSKARL_REFLECT_DAMAGE_PERCENT) / 100;
    }

    /**
     * BERSERKER: Oblicza karę do obrażeń (otrzymuje więcej dmg).
     */
    public static int applyBerserkerDamagePenalty(int damage) {
        return damage + (damage * GameConfig.BERSERKER_EXTRA_DAMAGE_TAKEN) / 100;
    }

    /**
     * TROPICIEL: Redukuje obrażenia magiczne (Mag/Runy).
     */
    public static int applyMagicResist(int damage) {
        return damage - (damage * GameConfig.TROPICIEL_MAGIC_RESIST) / 100;
    }



    /**
     * SKALD: Losuje pieśń (1-3).
     */
    public static int rollSkaldSong() {
        return random.nextInt(3) + 1;
    }

    /**
     * SKALD: Oblicza bonus obrażeń z Pieśni Odwagi.
     */
    public static int applySkaldDamageBoost(int damage) {
        return damage + (damage * GameConfig.SKALD_SONG_COURAGE_BOOST) / 100;
    }

    /**
     * SKALD: Oblicza leczenie z Pieśni Trola.
     */
    public static int calculateSkaldHeal(int maxHp) {
        return (maxHp * GameConfig.SKALD_SONG_TROLL_HEAL_PERCENT) / 100;
    }

    /**
     * SKALD: Oblicza pasywne obrażenia z Fałszowania.
     */
    public static int calculateSkaldPassiveDamage(int baseDamage) {
        return (baseDamage * GameConfig.SKALD_SONG_FALSE_PASSIVE_DMG) / 100;
    }
}