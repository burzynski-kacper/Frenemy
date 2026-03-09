package com.example.demo.service;

import com.example.demo.config.GameConfig;
import com.example.demo.dto.EnemyDTO;
import com.example.demo.dto.FightResultDTO;
import com.example.demo.dto.FightTurnDTO;
import com.example.demo.model.Character;
import com.example.demo.model.CombatState;
import com.example.demo.model.Enemy;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.EnemyRepository;
import com.example.demo.util.CombatCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CombatService {

    private final CharacterRepository characterRepository;
    private final EnemyRepository enemyRepository;
    private final LevelService levelService;

    /**
     * Main method handling the fight.
     */
    @Transactional
    public FightResultDTO fight(Long userId, Long enemyId) {
        Character player = characterRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Character not found."));

        Enemy enemy = enemyRepository.findById(enemyId)
                .orElseThrow(() -> new NoSuchElementException("Enemy not found."));

        // 1. Initialize HP and Combat State
        int playerMaxHp = CombatCalculator.calculatePlayerHp(player);
        int enemyMaxHp = enemy.getMaxHp();

        // Use external model class to maintain fluid HP state
        CombatState state = new CombatState(playerMaxHp, enemyMaxHp);

        List<FightTurnDTO> fightLog = new ArrayList<>();
        int turn = 0;
        boolean isPlayerTurn = true; // Player always goes first

        // Get ability codes (e.g. "BLOCK", "FRENZY")
        String playerAbility = player.getCharacterClass().getSpecialAbility();
        String enemyAbility = enemy.getAbility();

        // === 2. Main Fight Loop ===
        while (state.getPlayerHp() > 0 && state.getEnemyHp() > 0 && turn < 100) {
            turn++;

            if (isPlayerTurn) {
                if (state.isPlayerIsFrozen()) {
                    logFrozen(fightLog, turn, player.getName(), state.getEnemyHp());
                    state.setPlayerIsFrozen(false); // Reset freeze
                } else {
                    handlePlayerTurn(player, enemy, state, playerMaxHp, playerAbility, enemyAbility, turn, fightLog);
                }
            } else {
                if (state.isEnemyIsFrozen()) {
                    logFrozen(fightLog, turn, enemy.getName(), state.getPlayerHp());
                    state.setEnemyIsFrozen(false); // Reset freeze
                } else {
                    handleEnemyTurn(player, enemy, state, enemyMaxHp, playerAbility, enemyAbility, turn, fightLog);
                }
            }
            isPlayerTurn = !isPlayerTurn;
        }

        // 3. Build result and rewards
        return processFightResult(player, enemy, state, playerMaxHp, enemyMaxHp, fightLog, turn);
    }

    // ========================================================
    // PLAYER TURN
    // ========================================================

    private void handlePlayerTurn(Character player, Enemy enemy, CombatState state, int playerMaxHp,
            String playerAbility, String enemyAbility, int turn, List<FightTurnDTO> log) {
        switch (playerAbility) {
            case GameConfig.ABILITY_BERSERKER -> playerBerserkerAttack(player, enemy, state, enemyAbility, turn, log);
            case GameConfig.ABILITY_MISTRZ_RUN -> playerRuneAttack(player, enemy, state, enemyAbility, turn, log);
            case GameConfig.ABILITY_SKALD ->
                playerSkaldAttack(player, enemy, state, playerMaxHp, enemyAbility, turn, log);
            case GameConfig.ABILITY_WALKIRIA -> playerWalkiriaAttack(player, enemy, state, enemyAbility, turn, log);
            default -> playerNormalAttack(player, enemy, state, enemyAbility, turn, log);
        }
    }

    // --- Player Attack Implementations ---

    private void playerNormalAttack(Character player, Enemy enemy, CombatState state,
            String enemyAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculatePlayerDamage(player);
        boolean isCrit = CombatCalculator.isCriticalHit(player.getStats().getLuck());
        if (isCrit)
            damage = (int) (damage * 1.5);

        AttackResult result = tryDefense(damage, isCrit, enemyAbility);

        // If enemy dodges and is Walkira, absorbs 50% of damage
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
        }

        // Enemy blocks and reflects damage (Huskarl Enemy)
        if (result.blocked && result.reflected > 0) {
            state.setPlayerHp(state.getPlayerHp() - result.reflected);
            log.add(buildTurn(turn, player.getName(), "REFLECT_TAKEN", 0, state.getEnemyHp(),
                    enemy.getName() + " BLOCKS and REFLECTS " + result.reflected + " dmg to you!"));
        }

        state.setEnemyHp(state.getEnemyHp() - result.damage);
        logAttack(turn, player.getName(), enemy.getName(), result, isCrit, state.getEnemyHp(), log, "");
    }

    private void playerBerserkerAttack(Character player, Enemy enemy, CombatState state,
            String enemyAbility, int turn, List<FightTurnDTO> log) {
        int totalDamage = 0;
        int attacks = 0;
        List<String> details = new ArrayList<>();

        do {
            attacks++;
            int damage = CombatCalculator.calculatePlayerDamage(player);
            boolean isCrit = CombatCalculator.isCriticalHit(player.getStats().getLuck());
            if (isCrit)
                damage = (int) (damage * 1.5);

            AttackResult result = tryDefense(damage, isCrit, enemyAbility);

            if (result.blocked) {
                details.add("BLOCK");
                if (result.reflected > 0) {
                    state.setPlayerHp(state.getPlayerHp() - result.reflected);
                    details.add("(Reflected " + result.reflected + ")");
                }
            } else if (result.dodged) {
                details.add("DODGE");
                // Enemy dodges and absorbs 50% of damage
                if (result.givesDiveBonus) {
                    int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
                    state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
                }
            } else {
                totalDamage += result.damage;
                details.add(String.valueOf(result.damage));
            }

            state.setEnemyHp(state.getEnemyHp() - result.damage);
            if (state.getEnemyHp() <= 0)
                break;

        } while (attacks < GameConfig.BERSERKER_MAX_CHAIN_ATTACKS &&
                CombatCalculator.checkChance(GameConfig.BERSERKER_CHAIN_ATTACK_CHANCE));

        String action;
        String desc;

        if (attacks > 1) {
            action = "FRENZY";
            desc = player.getName() + " enters Berserker's Fury! Series of " + attacks + " attacks ["
                    + String.join(", ", details)
                    + "]";
        } else {
            action = "ATTACK";
            desc = player.getName() + " performs a brutal strike [" + details.get(0) + "]";
        }

        log.add(buildTurn(turn, player.getName(), action, totalDamage, Math.max(0, state.getEnemyHp()), desc));
    }

    private void playerRuneAttack(Character player, Enemy enemy, CombatState state, String enemyAbility,
            int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculatePlayerDamage(player);
        boolean isCrit = CombatCalculator.isCriticalHit(player.getStats().getLuck());

        // Special interaction: Hunter (Enemy) has magic resistance
        if (GameConfig.ABILITY_TROPICIEL.equals(enemyAbility)) {
            int reducedDamage = CombatCalculator.applyMagicResist(damage);
            state.setEnemyHp(state.getEnemyHp() - reducedDamage);
            log.add(buildTurn(turn, player.getName(), "RUNE_RESIST", reducedDamage, Math.max(0, state.getEnemyHp()),
                    player.getName() + " casts a rune, but the enemy's amulet protects! (" + damage + " -> "
                            + reducedDamage
                            + ")"));
            return;
        }

        if (isCrit) {
            damage = (int) (damage * 1.5);
            if (CombatCalculator.checkChance(GameConfig.MISTRZ_RUN_FREEZE_CHANCE)) {
                state.setEnemyIsFrozen(true);
                state.setEnemyHp(state.getEnemyHp() - damage);
                log.add(buildTurn(turn, player.getName(), "FREEZE", damage, Math.max(0, state.getEnemyHp()),
                        player.getName() + " FREEZES the enemy with a FROST RUNE!"));
                return;
            }
        }

        state.setEnemyHp(state.getEnemyHp() - damage);
        log.add(buildTurn(turn, player.getName(), "RUNE", damage, Math.max(0, state.getEnemyHp()),
                player.getName() + " casts a rune for " + damage + " (unblockable)."));
    }

    private void playerSkaldAttack(Character player, Enemy enemy, CombatState state, int playerMaxHp,
            String enemyAbility, int turn, List<FightTurnDTO> log) {
        int songRoll = CombatCalculator.rollSkaldSong();
        int damage = CombatCalculator.calculatePlayerDamage(player);
        String desc;
        int finalDamage;
        AttackResult result;

        switch (songRoll) {
            case 1 -> { // Courage
                int boostedDmg = CombatCalculator.applySkaldDamageBoost(damage);
                result = tryDefense(boostedDmg, false, enemyAbility);
                finalDamage = result.damage;
                desc = player.getName() + " sings the Courage Song! Deals " + finalDamage;
                if (result.blocked)
                    desc += " (Blocked!)";
                if (result.dodged)
                    desc += " (Dodged!)";
                if (result.reflected > 0)
                    state.setPlayerHp(state.getPlayerHp() - result.reflected);
            }
            case 2 -> { // Troll
                int heal = CombatCalculator.calculateSkaldHeal(playerMaxHp);
                state.setPlayerHp(Math.min(playerMaxHp, state.getPlayerHp() + heal));

                result = tryDefense(damage, false, enemyAbility);
                finalDamage = result.damage;
                desc = player.getName() + " sings the Troll Song! Heals for " + heal + " HP and deals " + finalDamage;
                if (result.reflected > 0)
                    state.setPlayerHp(state.getPlayerHp() - result.reflected);
            }
            default -> { // False
                int passiveDmg = CombatCalculator.calculateSkaldPassiveDamage(damage);
                result = tryDefense(damage, false, enemyAbility);
                finalDamage = result.damage + passiveDmg;
                desc = player.getName() + " sings the False Song! Deals " + finalDamage;
                if (result.reflected > 0)
                    state.setPlayerHp(state.getPlayerHp() - result.reflected);
            }
        }

        // Check if enemy dodged the physical part of the attack
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
        }

        state.setEnemyHp(state.getEnemyHp() - finalDamage);
        log.add(buildTurn(turn, player.getName(), "SONG", finalDamage, Math.max(0, state.getEnemyHp()), desc));
    }

    private void playerWalkiriaAttack(Character player, Enemy enemy, CombatState state,
            String enemyAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculatePlayerDamage(player);
        boolean isCrit = CombatCalculator.isCriticalHit(player.getStats().getLuck());
        String action = "ATTACK";
        String descPrefix = "";

        // Check for absorbed player damage
        if (state.getPlayerAbsorbedDamage() > 0) {
            damage += state.getPlayerAbsorbedDamage(); // Add absorbed damage
            descPrefix = "[COUNTER +" + state.getPlayerAbsorbedDamage() + " dmg] ";
            state.setPlayerAbsorbedDamage(0); // Reset absorbed damage
            action = "DIVE";
        } else if (isCrit) {
            damage = (int) (damage * 1.5);
        }

        AttackResult result = tryDefense(damage, isCrit || "DIVE".equals(action), enemyAbility);

        // If enemy dodges, absorbs half of the damage
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
        }
        // -----------------------------------------------------

        if (result.blocked && result.reflected > 0) {
            state.setPlayerHp(state.getPlayerHp() - result.reflected);
            descPrefix += "(Reflected " + result.reflected + ") ";
        }

        state.setEnemyHp(state.getEnemyHp() - result.damage);
        logAttack(turn, player.getName(), enemy.getName(), result, isCrit, state.getEnemyHp(), log, descPrefix);
    }

    // ========================================================
    // ENEMY TURN
    // ========================================================

    private void handleEnemyTurn(Character player, Enemy enemy, CombatState state, int enemyMaxHp,
            String playerAbility, String enemyAbility, int turn, List<FightTurnDTO> log) {
        if (enemyAbility == null) {
            enemyNormalAttack(player, enemy, state, playerAbility, turn, log);
            return;
        }

        switch (enemyAbility) {
            case GameConfig.ABILITY_BERSERKER -> enemyBerserkerAttack(player, enemy, state, playerAbility, turn, log);
            case GameConfig.ABILITY_MISTRZ_RUN -> enemyRuneAttack(player, enemy, state, playerAbility, turn, log);
            case GameConfig.ABILITY_SKALD ->
                enemySkaldAttack(player, enemy, state, enemyMaxHp, playerAbility, turn, log);
            case GameConfig.ABILITY_WALKIRIA -> enemyWalkiriaAttack(player, enemy, state, playerAbility, turn, log);
            default -> enemyNormalAttack(player, enemy, state, playerAbility, turn, log);
        }
    }

    // --- Enemy Attacks ---

    private void enemyNormalAttack(Character player, Enemy enemy, CombatState state,
            String playerAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        boolean isCrit = CombatCalculator.isCriticalHit(enemy.getLuck());
        if (isCrit)
            damage = (int) (damage * 1.5);

        AttackResult result = tryDefense(damage, isCrit, playerAbility);

        // Handle reflection (Player Huskarl)
        if (result.blocked && result.reflected > 0) {
            state.setEnemyHp(state.getEnemyHp() - result.reflected);
            log.add(buildTurn(turn, player.getName(), "REFLECT", 0, state.getEnemyHp(),
                    player.getName() + " BLOCKS the enemy's attack and REFLECTS " + result.reflected + " damage!"));
        }

        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed); // Player Walkiria absorbs
        }

        state.setPlayerHp(state.getPlayerHp() - result.damage);
        logAttack(turn, enemy.getName(), player.getName(), result, isCrit, state.getPlayerHp(), log, "");
    }

    private void enemyBerserkerAttack(Character player, Enemy enemy, CombatState state,
            String playerAbility, int turn, List<FightTurnDTO> log) {
        int totalDamage = 0;
        int attacks = 0;
        List<String> details = new ArrayList<>();

        do {
            attacks++;
            int damage = CombatCalculator.calculateEnemyDamage(enemy);
            boolean isCrit = CombatCalculator.isCriticalHit(enemy.getLuck());
            if (isCrit)
                damage = (int) (damage * 1.5);

            AttackResult result = tryDefense(damage, isCrit, playerAbility);

            if (result.blocked) {
                details.add("BLOCK");
                if (result.reflected > 0) {
                    state.setEnemyHp(state.getEnemyHp() - result.reflected);
                    details.add("(REFLECTED " + result.reflected + ")");
                }
            } else if (result.dodged) {
                details.add("DODGE");
                if (result.givesDiveBonus) {
                    int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
                    state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed);
                }
            } else {
                totalDamage += result.damage;
                details.add(String.valueOf(result.damage));
            }

            state.setPlayerHp(state.getPlayerHp() - result.damage);
            if (state.getPlayerHp() <= 0)
                break;

        } while (attacks < GameConfig.BERSERKER_MAX_CHAIN_ATTACKS &&
                CombatCalculator.checkChance(GameConfig.BERSERKER_CHAIN_ATTACK_CHANCE));

        String action;
        String desc;

        if (attacks > 1) {
            action = "FRENZY";
            desc = enemy.getName() + " enters FURY! Series of " + attacks + " hits [" + String.join(", ", details)
                    + "]";
        } else {
            action = "ATTACK";
            desc = enemy.getName() + " performs a brutal strike [" + details.get(0) + "]";
        }

        log.add(buildTurn(turn, enemy.getName(), action, totalDamage, Math.max(0, state.getPlayerHp()), desc));
    }

    private void enemyRuneAttack(Character player, Enemy enemy, CombatState state,
            String playerAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        boolean isCrit = CombatCalculator.isCriticalHit(enemy.getLuck());

        // Hunter Player vs Magic Enemy
        if (GameConfig.ABILITY_TROPICIEL.equals(playerAbility)) {
            int reducedDamage = CombatCalculator.applyMagicResist(damage);
            state.setPlayerHp(state.getPlayerHp() - reducedDamage);
            log.add(buildTurn(turn, enemy.getName(), "RUNE_RESIST", reducedDamage, Math.max(0, state.getPlayerHp()),
                    enemy.getName() + " casts a rune, but your amulet protects you! (" + damage + " -> " + reducedDamage
                            + ")"));
            return;
        }

        if (isCrit) {
            damage = (int) (damage * 1.5);
            if (CombatCalculator.checkChance(GameConfig.MISTRZ_RUN_FREEZE_CHANCE)) {
                state.setPlayerIsFrozen(true);
                state.setPlayerHp(state.getPlayerHp() - damage);
                log.add(buildTurn(turn, enemy.getName(), "FREEZE", damage, Math.max(0, state.getPlayerHp()),
                        enemy.getName() + " casts a frost rune and freezes you!"));
                return;
            }
        }

        state.setPlayerHp(state.getPlayerHp() - damage);
        log.add(buildTurn(turn, enemy.getName(), "RUNE", damage, Math.max(0, state.getPlayerHp()),
                "True damage from a magic rune hits you!"));
    }

    private void enemySkaldAttack(Character player, Enemy enemy, CombatState state, int enemyMaxHp,
            String playerAbility, int turn, List<FightTurnDTO> log) {
        int songRoll = CombatCalculator.rollSkaldSong();
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        String desc;
        int finalDamage;
        AttackResult result;

        switch (songRoll) {
            case 1 -> { // Courage
                int boostedDmg = CombatCalculator.applySkaldDamageBoost(damage);
                result = tryDefense(boostedDmg, false, playerAbility); // True damage
                finalDamage = result.damage;
                desc = enemy.getName() + " sings the Courage! Attack: " + finalDamage;
                if (result.blocked && result.reflected > 0)
                    state.setEnemyHp(state.getEnemyHp() - result.reflected);
            }
            case 2 -> { // Heal
                int heal = CombatCalculator.calculateSkaldHeal(enemyMaxHp);
                state.setEnemyHp(Math.min(enemyMaxHp, state.getEnemyHp() + heal));

                result = tryDefense(damage, false, playerAbility); // True damage
                finalDamage = result.damage;
                desc = enemy.getName() + " sings the Heal! Attack: " + finalDamage;
                if (result.blocked && result.reflected > 0)
                    state.setEnemyHp(state.getEnemyHp() - result.reflected);
            }
            default -> { // False
                int passiveDmg = CombatCalculator.calculateSkaldPassiveDamage(damage);
                result = tryDefense(damage, false, playerAbility); // True damage
                finalDamage = result.damage + passiveDmg;
                desc = enemy.getName() + " sings the False! Passive damage + attack.";
                if (result.blocked && result.reflected > 0)
                    state.setEnemyHp(state.getEnemyHp() - result.reflected);
            }
        }

        // Now we correctly check if we dodge (using the true attack result)
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed);
        }

        state.setPlayerHp(state.getPlayerHp() - finalDamage);
        log.add(buildTurn(turn, enemy.getName(), "SONG", finalDamage, Math.max(0, state.getPlayerHp()), desc));
    }

    private void enemyWalkiriaAttack(Character player, Enemy enemy, CombatState state,
            String playerAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        boolean isCrit = CombatCalculator.isCriticalHit(enemy.getLuck());
        String action = "ATTACK";
        String descPrefix = "";

        // Check for absorbed damage from Walkiria
        if (state.getEnemyAbsorbedDamage() > 0) {
            damage += state.getEnemyAbsorbedDamage(); // Add absorbed damage
            descPrefix = "[PIKOWANIE +" + state.getEnemyAbsorbedDamage() + " dmg] ";
            state.setEnemyAbsorbedDamage(0); // Reset absorbed damage
            action = "DIVE";
        } else if (isCrit) {
            damage = (int) (damage * 1.5);
        }

        AttackResult result = tryDefense(damage, isCrit || "DIVE".equals(action), playerAbility);

        if (result.blocked && result.reflected > 0) {
            state.setEnemyHp(state.getEnemyHp() - result.reflected);
            descPrefix += "(REFLECTED " + result.reflected + ") ";
        }
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed);
        }

        state.setPlayerHp(state.getPlayerHp() - result.damage);
        logAttack(turn, enemy.getName(), player.getName(), result, isCrit, state.getPlayerHp(), log, descPrefix);
    }

    // ========================================================
    // DEFENSE SYSTEM
    // ========================================================

    private AttackResult tryDefense(int damage, boolean isCrit, String defenderAbility) {
        AttackResult result = new AttackResult();
        result.damage = damage;
        result.originalDamage = damage; // Save original damage

        if (defenderAbility == null)
            return result;

        switch (defenderAbility) {
            case GameConfig.ABILITY_HUSKARL -> {
                if (CombatCalculator.checkChance(GameConfig.HUSKARL_BLOCK_CHANCE)) {
                    result.blocked = true;
                    result.damage = 0;
                    if (isCrit) {
                        result.reflected = CombatCalculator.calculateReflectedDamage(damage);
                    }
                }
            }
            case GameConfig.ABILITY_BERSERKER -> {
                result.damage = CombatCalculator.applyBerserkerDamagePenalty(damage);
            }
            case GameConfig.ABILITY_TROPICIEL -> {
                if (CombatCalculator.checkChance(GameConfig.TROPICIEL_DODGE_CHANCE)) {
                    result.dodged = true;
                    result.damage = 0;
                }
            }
            case GameConfig.ABILITY_WALKIRIA -> {
                if (CombatCalculator.checkChance(GameConfig.WALKIRIA_DODGE_CHANCE)) {
                    result.dodged = true;
                    result.damage = 0;
                    result.givesDiveBonus = true;
                }
            }
        }
        return result;
    }

    // ========================================================
    // HELPER METHODS
    // ========================================================

    private FightResultDTO processFightResult(Character player, Enemy enemy, CombatState state,
            int pMax, int eMax, List<FightTurnDTO> log, int turns) {
        boolean playerWon = state.getEnemyHp() <= 0;
        int xpGained = 0;
        int goldGained = 0;
        boolean leveledUp = false;
        int newLevel = player.getLevel();

        if (playerWon) {
            xpGained = enemy.getRewardXp();
            goldGained = enemy.getRewardGold();
            player.setExperience(player.getExperience() + xpGained);
            player.setGold(player.getGold() + goldGained);

            int levelsGained = levelService.checkAndLevelUp(player);
            if (levelsGained > 0) {
                leveledUp = true;
                newLevel = player.getLevel();
            }
            characterRepository.save(player);
        }

        return FightResultDTO.builder()
                .playerWon(playerWon)
                .playerName(player.getName())
                .enemyName(enemy.getName())
                .playerHpStart(pMax)
                .playerHpEnd(Math.max(0, state.getPlayerHp()))
                .enemyHpStart(eMax)
                .enemyHpEnd(Math.max(0, state.getEnemyHp()))
                .turnsCount(turns)
                .fightLog(log)
                .xpGained(xpGained)
                .goldGained(goldGained)
                .leveledUp(leveledUp)
                .newLevel(newLevel)
                .build();
    }

    private void logFrozen(List<FightTurnDTO> log, int turn, String name, int hp) {
        log.add(buildTurn(turn, name, "FROZEN", 0, hp, name + " is frozen and loses a turn!"));
    }

    private void logAttack(int turn, String attacker, String target, AttackResult result, boolean isCrit, int targetHp,
            List<FightTurnDTO> log, String prefix) {
        String action = "ATTACK";
        String desc = prefix + attacker + " attacks.";

        if (result.blocked) {
            action = "BLOCKED";
            desc = prefix + target + " BLOCKS the attack!";
            if (result.reflected > 0)
                desc += " (REFLECTED!)";
        } else if (result.dodged) {
            action = "DODGED";
            desc = prefix + target + " dodges!";
        } else {
            if (isCrit) {
                action = "CRITICAL";
                desc = prefix + attacker + " deals CRITICAL damage! (" + result.damage + ")";
            } else {
                desc = prefix + attacker + " hits for " + result.damage + ".";
            }
        }

        log.add(buildTurn(turn, attacker, action, result.damage, Math.max(0, targetHp), desc));
    }

    private FightTurnDTO buildTurn(int turn, String attacker, String action, int damage, int targetHp, String desc) {
        return FightTurnDTO.builder()
                .turnNumber(turn)
                .attacker(attacker)
                .action(action)
                .damage(damage)
                .targetHpAfter(targetHp)
                .description(desc)
                .build();
    }

    // Inner class to handle attack result
    private static class AttackResult {
        int damage = 0;
        int originalDamage = 0; // Original damage before defense (for Walkiria absorption)
        boolean blocked = false;
        boolean dodged = false;
        boolean givesDiveBonus = false;
        int reflected = 0;
    }

    public List<EnemyDTO> getEnemiesAroundLevel(int playerLevel) {
        // Business logic for calculating the range (e.g., +/- 2 level)
        int minLevel = Math.max(1, playerLevel - 2);
        int maxLevel = playerLevel + 2;

        List<Enemy> enemies = enemyRepository.findByLevelBetween(minLevel, maxLevel);

        return enemies.stream()
                .map(EnemyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EnemyDTO> getAllEnemies() {
        return enemyRepository.findAll()
                .stream()
                .map(EnemyDTO::fromEntity) // Using our mapping method
                .collect(Collectors.toList());
    }
}