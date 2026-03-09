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

    // === CLASS: HUSKARL (Tank) ===
    public static final String ABILITY_HUSKARL = "BLOCK";
    public static final int HUSKARL_BLOCK_CHANCE = 25; // 25% chance to block
    public static final int HUSKARL_REFLECT_DAMAGE_PERCENT = 10; // Reflect 10% damage (only on critical block)

    // === CLASS: BERSERKER (DPS) ===
    public static final String ABILITY_BERSERKER = "FRENZY";
    public static final int BERSERKER_CHAIN_ATTACK_CHANCE = 50; // 50% chance for next attack
    public static final int BERSERKER_MAX_CHAIN_ATTACKS = 6; // Max 6 hits
    public static final int BERSERKER_EXTRA_DAMAGE_TAKEN = 10; // Receives 10% more damage (no armor)

    // === CLASS: HUNTER (Dodge) ===
    public static final String ABILITY_TROPICIEL = "DODGE";
    public static final int TROPICIEL_DODGE_CHANCE = 50; // 50% chance to dodge
    public static final int TROPICIEL_MAGIC_RESIST = 20; // 20% magic resistance (Run)

    // === CLASS: WALKIRIA (Counter) ===
    public static final String ABILITY_WALKIRIA = "COUNTER";
    public static final int WALKIRIA_DODGE_CHANCE = 40; // 40% chance to dodge
    public static final int WALKIRIA_DAMAGE_ABSORPTION_PERCENT = 50; // Absorbs 50% of dodged damage as bonus to attack

    // === CLASS: RUNE MASTER (Mage) ===
    public static final String ABILITY_MISTRZ_RUN = "TRUE_STRIKE";
    public static final int MISTRZ_RUN_FREEZE_CHANCE = 10; // 10% chance to freeze on critical strike

    // === CLASS: SKALD (Paladin) ===
    public static final String ABILITY_SKALD = "SHAPESHIFT"; // Changes to song form
    public static final int SKALD_SONG_COURAGE_BOOST = 25; // +25% damage in this turn
    public static final int SKALD_SONG_TROLL_HEAL_PERCENT = 10; // Heals 10% max HP
    public static final int SKALD_SONG_FALSE_PASSIVE_DMG = 20; // Additional 20% damage from "pain in the ear"
}