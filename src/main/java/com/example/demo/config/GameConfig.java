package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {

    // === LEVEL SYSTEM ===
    public static final int MAX_LEVEL = 100;
    public static final int STAT_POINTS_PER_LEVEL = 5;
    public static final int BASE_XP_MULTIPLIER = 100;

    // === STARTING VALUES ===
    public static final int STARTING_GOLD = 10;
    public static final int STARTING_LEVEL = 1;
    public static final long STARTING_EXPERIENCE = 0;
    public static final int BASE_STAT_VALUE = 10;

    // === KLASA: HUSKARL (Tank) ===
    public static final String ABILITY_HUSKARL = "BLOCK";
    public static final int HUSKARL_BLOCK_CHANCE = 25;           // 25% szansy na całkowity blok
    public static final int HUSKARL_REFLECT_DAMAGE_PERCENT = 10; // Odbija 10% obrażeń (tylko przy bloku krytyka)

    // === KLASA: BERSERKER (DPS) ===
    public static final String ABILITY_BERSERKER = "FRENZY";
    public static final int BERSERKER_CHAIN_ATTACK_CHANCE = 50;  // 50% na kolejny atak
    public static final int BERSERKER_MAX_CHAIN_ATTACKS = 6;     // Max 6 ciosów
    public static final int BERSERKER_EXTRA_DAMAGE_TAKEN = 10;   // Otrzymuje 10% więcej obrażeń (brak zbroi)

    // === KLASA: WILCZY TROPICIEL (Unik) ===
    public static final String ABILITY_TROPICIEL = "DODGE";
    public static final int TROPICIEL_DODGE_CHANCE = 50;         // 50% szansy na unik fizyczny
    public static final int TROPICIEL_MAGIC_RESIST = 20;         // 20% redukcji obrażeń od Magii (Run)

    // === KLASA: WALKIRIA (Hybryda) ===
    public static final String ABILITY_WALKIRIA = "DODGE_WALKIRIA";
    public static final int WALKIRIA_DODGE_CHANCE = 40;          // 40% szansy na unik
    public static final int WALKIRIA_DAMAGE_ABSORPTION_PERCENT = 50;  // Absorbuje 50% unikniętych obrażeń jako bonus do ataku

    // === KLASA: MISTRZ RUN (Mag) ===
    public static final String ABILITY_MISTRZ_RUN = "TRUE_STRIKE";
    public static final int MISTRZ_RUN_FREEZE_CHANCE = 10;       // 10% szansy na zamrożenie przy ataku krytycznym

    // === KLASA: SKALD (Chaos/Support) ===
    public static final String ABILITY_SKALD = "SHAPESHIFT";     // Mechanicznie to zmiana formy pieśni
    public static final int SKALD_SONG_COURAGE_BOOST = 25;       // +25% obrażeń w tej turze
    public static final int SKALD_SONG_TROLL_HEAL_PERCENT = 10;  // Leczy 10% max HP
    public static final int SKALD_SONG_FALSE_PASSIVE_DMG = 20;   // Dodatkowe 20% obrażeń od "bólu uszu"
}