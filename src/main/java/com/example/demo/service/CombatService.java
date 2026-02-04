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
     * Główna metoda obsługująca walkę.
     */
    @Transactional
    public FightResultDTO fight(Long userId, Long enemyId) {
        Character player = characterRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono postaci gracza."));

        Enemy enemy = enemyRepository.findById(enemyId)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono przeciwnika."));

        // 1. Inicjalizacja HP i Stanu Walki
        int playerMaxHp = CombatCalculator.calculatePlayerHp(player);
        int enemyMaxHp = enemy.getMaxHp();

        // Używamy zewnętrznej klasy modelu do trzymania płynnego stanu HP
        CombatState state = new CombatState(playerMaxHp, enemyMaxHp);

        List<FightTurnDTO> fightLog = new ArrayList<>();
        int turn = 0;
        boolean isPlayerTurn = true; // Gracz zawsze zaczyna

        // Pobranie kodów umiejętności (np. "BLOCK", "FRENZY")
        String playerAbility = player.getCharacterClass().getSpecialAbility();
        String enemyAbility = enemy.getAbility();

        // === 2. GŁÓWNA PĘTLA WALKI ===
        while (state.getPlayerHp() > 0 && state.getEnemyHp() > 0 && turn < 100) {
            turn++;

            if (isPlayerTurn) {
                if (state.isPlayerIsFrozen()) {
                    logFrozen(fightLog, turn, player.getName(), state.getEnemyHp());
                    state.setPlayerIsFrozen(false); // Reset zamrożenia
                } else {
                    handlePlayerTurn(player, enemy, state, playerMaxHp, playerAbility, enemyAbility, turn, fightLog);
                }
            } else {
                if (state.isEnemyIsFrozen()) {
                    logFrozen(fightLog, turn, enemy.getName(), state.getPlayerHp());
                    state.setEnemyIsFrozen(false); // Reset zamrożenia
                } else {
                    handleEnemyTurn(player, enemy, state, enemyMaxHp, playerAbility, enemyAbility, turn, fightLog);
                }
            }
            isPlayerTurn = !isPlayerTurn;
        }

        // 3. Budowanie wyniku i nagrody
        return processFightResult(player, enemy, state, playerMaxHp, enemyMaxHp, fightLog, turn);
    }

    // ========================================================
    // TURA GRACZA (Router)
    // ========================================================

    private void handlePlayerTurn(Character player, Enemy enemy, CombatState state, int playerMaxHp,
                                  String playerAbility, String enemyAbility, int turn, List<FightTurnDTO> log) {
        switch (playerAbility) {
            case GameConfig.ABILITY_BERSERKER -> playerBerserkerAttack(player, enemy, state, enemyAbility, turn, log);
            case GameConfig.ABILITY_MISTRZ_RUN -> playerRuneAttack(player, enemy, state, enemyAbility, turn, log);
            case GameConfig.ABILITY_SKALD -> playerSkaldAttack(player, enemy, state, playerMaxHp, enemyAbility, turn, log);
            case GameConfig.ABILITY_WALKIRIA -> playerWalkiriaAttack(player, enemy, state, enemyAbility, turn, log);
            default -> playerNormalAttack(player, enemy, state, enemyAbility, turn, log);
        }
    }

    // --- Implementacje Ataków Gracza ---

    private void playerNormalAttack(Character player, Enemy enemy, CombatState state,
                                    String enemyAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculatePlayerDamage(player);
        boolean isCrit = CombatCalculator.isCriticalHit(player.getStats().getLuck());
        if (isCrit) damage = (int)(damage * 1.5);

        AttackResult result = tryDefense(damage, isCrit, enemyAbility);

        // Jeśli wróg zrobił unik i jest Walkirią, absorbuje 50% obrażeń
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
        }

        // Obsługa odbicia (Huskarl Enemy)
        if (result.blocked && result.reflected > 0) {
            state.setPlayerHp(state.getPlayerHp() - result.reflected);
            log.add(buildTurn(turn, player.getName(), "REFLECT_TAKEN", 0, state.getEnemyHp(),
                    enemy.getName() + " BLOKUJE i ODBIJA " + result.reflected + " dmg w Twoją stronę!"));
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
            if (isCrit) damage = (int)(damage * 1.5);

            AttackResult result = tryDefense(damage, isCrit, enemyAbility);

            if (result.blocked) {
                details.add("BLOK");
                if (result.reflected > 0) {
                    state.setPlayerHp(state.getPlayerHp() - result.reflected);
                    details.add("(Odbito " + result.reflected + ")");
                }
            } else if (result.dodged) {
                details.add("UNIK");
                // Wróg unika ciosu szału -> absorbuje połowę obrażeń
                if (result.givesDiveBonus) {
                    int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
                    state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
                }
            } else {
                totalDamage += result.damage;
                details.add(String.valueOf(result.damage));
            }

            state.setEnemyHp(state.getEnemyHp() - result.damage);
            if (state.getEnemyHp() <= 0) break;

        } while (attacks < GameConfig.BERSERKER_MAX_CHAIN_ATTACKS &&
                CombatCalculator.checkChance(GameConfig.BERSERKER_CHAIN_ATTACK_CHANCE));

        String action;
        String desc;

        if (attacks > 1) {
            action = "FRENZY";
            desc = player.getName() + " wpada w SZAŁ! Seria " + attacks + " ciosów [" + String.join(", ", details) + "]";
        } else {
            action = "ATTACK";
            desc = player.getName() + " wykonuje brutalny cios [" + details.get(0) + "]";
        }

        log.add(buildTurn(turn, player.getName(), action, totalDamage, Math.max(0, state.getEnemyHp()), desc));
    }

    private void playerRuneAttack(Character player, Enemy enemy, CombatState state, String enemyAbility,
                                  int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculatePlayerDamage(player);
        boolean isCrit = CombatCalculator.isCriticalHit(player.getStats().getLuck());

        // Specjalna interakcja: Tropiciel (Enemy) ma odporność na magię
        if (GameConfig.ABILITY_TROPICIEL.equals(enemyAbility)) {
            int reducedDamage = CombatCalculator.applyMagicResist(damage);
            state.setEnemyHp(state.getEnemyHp() - reducedDamage);
            log.add(buildTurn(turn, player.getName(), "RUNE_RESIST", reducedDamage, Math.max(0, state.getEnemyHp()),
                    player.getName() + " rzuca runę, ale amulet wroga chroni! (" + damage + " -> " + reducedDamage + ")"));
            return;
        }

        if (isCrit) {
            damage = (int)(damage * 1.5);
            if (CombatCalculator.checkChance(GameConfig.MISTRZ_RUN_FREEZE_CHANCE)) {
                state.setEnemyIsFrozen(true);
                state.setEnemyHp(state.getEnemyHp() - damage);
                log.add(buildTurn(turn, player.getName(), "FREEZE", damage, Math.max(0, state.getEnemyHp()),
                        player.getName() + " ZAMRAŻA wroga Lodową Runą!"));
                return;
            }
        }

        state.setEnemyHp(state.getEnemyHp() - damage);
        log.add(buildTurn(turn, player.getName(), "RUNE", damage, Math.max(0, state.getEnemyHp()),
                player.getName() + " ciska runą za " + damage + " (nie do obrony)."));
    }

    private void playerSkaldAttack(Character player, Enemy enemy, CombatState state, int playerMaxHp,
                                   String enemyAbility, int turn, List<FightTurnDTO> log) {
        int songRoll = CombatCalculator.rollSkaldSong();
        int damage = CombatCalculator.calculatePlayerDamage(player);
        String desc;
        int finalDamage;
        AttackResult result; // Zmienna pomocnicza

        switch (songRoll) {
            case 1 -> { // Odwaga
                int boostedDmg = CombatCalculator.applySkaldDamageBoost(damage);
                result = tryDefense(boostedDmg, false, enemyAbility);
                finalDamage = result.damage;
                desc = player.getName() + " ryczy PIEŚŃ ODWAGI! Uderza potężniej za " + finalDamage;
                if(result.blocked) desc += " (Blok!)";
                if(result.dodged) desc += " (Unik!)";
                if(result.reflected > 0) state.setPlayerHp(state.getPlayerHp() - result.reflected);
            }
            case 2 -> { // Troll
                int heal = CombatCalculator.calculateSkaldHeal(playerMaxHp);
                state.setPlayerHp(Math.min(playerMaxHp, state.getPlayerHp() + heal));

                result = tryDefense(damage, false, enemyAbility);
                finalDamage = result.damage;
                desc = player.getName() + " nuci PIEŚŃ TROLA (+ " + heal + " HP) i uderza.";
                if(result.reflected > 0) state.setPlayerHp(state.getPlayerHp() - result.reflected);
            }
            default -> { // Fałszowanie
                int passiveDmg = CombatCalculator.calculateSkaldPassiveDamage(damage);
                result = tryDefense(damage, false, enemyAbility);
                finalDamage = result.damage + passiveDmg;
                desc = player.getName() + " FAŁSZUJE! Pasywne " + passiveDmg + " + atak " + result.damage;
                if(result.reflected > 0) state.setPlayerHp(state.getPlayerHp() - result.reflected);
            }
        }

        // Sprawdzamy czy wróg uniknął fizycznej części ataku
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

        // SPRAWDZENIE ZAABSORBOWANYCH OBRAŻEŃ GRACZA
        if (state.getPlayerAbsorbedDamage() > 0) {
            damage += state.getPlayerAbsorbedDamage();  // Dodaj zaabsorbowane obrażenia
            descPrefix = "[PIKOWANIE +" + state.getPlayerAbsorbedDamage() + " dmg] ";
            state.setPlayerAbsorbedDamage(0); // Reset zaabsorbowanych obrażeń
            action = "DIVE";
        } else if (isCrit) {
            damage = (int)(damage * 1.5);
        }

        AttackResult result = tryDefense(damage, isCrit || "DIVE".equals(action), enemyAbility);

        // Jeśli wróg zrobił unik, absorbuje połowę obrażeń
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setEnemyAbsorbedDamage(state.getEnemyAbsorbedDamage() + absorbed);
        }
        // -----------------------------------------------------

        if (result.blocked && result.reflected > 0) {
            state.setPlayerHp(state.getPlayerHp() - result.reflected);
            descPrefix += "(Odbito " + result.reflected + ") ";
        }

        state.setEnemyHp(state.getEnemyHp() - result.damage);
        logAttack(turn, player.getName(), enemy.getName(), result, isCrit, state.getEnemyHp(), log, descPrefix);
    }

    // ========================================================
    // TURA PRZECIWNIKA (Router)
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
            case GameConfig.ABILITY_SKALD -> enemySkaldAttack(player, enemy, state, enemyMaxHp, playerAbility, turn, log);
            case GameConfig.ABILITY_WALKIRIA -> enemyWalkiriaAttack(player, enemy, state, playerAbility, turn, log);
            default -> enemyNormalAttack(player, enemy, state, playerAbility, turn, log);
        }
    }

    // --- Implementacje Ataków Przeciwnika ---

    private void enemyNormalAttack(Character player, Enemy enemy, CombatState state,
                                   String playerAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        boolean isCrit = CombatCalculator.isCriticalHit(enemy.getLuck());
        if (isCrit) damage = (int)(damage * 1.5);

        AttackResult result = tryDefense(damage, isCrit, playerAbility);

        // Obsługa odbicia (Huskarl Player)
        if (result.blocked && result.reflected > 0) {
            state.setEnemyHp(state.getEnemyHp() - result.reflected);
            log.add(buildTurn(turn, player.getName(), "REFLECT", 0, state.getEnemyHp(),
                    player.getName() + " BLOKUJE krytyka i ODBIJA " + result.reflected + " obrażeń!"));
        }

        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed); // Gracz Walkiria absorbuje obrażenia
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
            if (isCrit) damage = (int)(damage * 1.5);

            AttackResult result = tryDefense(damage, isCrit, playerAbility);

            if (result.blocked) {
                details.add("BLOK");
                if (result.reflected > 0) {
                    state.setEnemyHp(state.getEnemyHp() - result.reflected);
                    details.add("(Odbito " + result.reflected + ")");
                }
            } else if (result.dodged) {
                details.add("UNIK");
                if (result.givesDiveBonus) {
                    int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
                    state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed);
                }
            } else {
                totalDamage += result.damage;
                details.add(String.valueOf(result.damage));
            }

            state.setPlayerHp(state.getPlayerHp() - result.damage);
            if (state.getPlayerHp() <= 0) break;

        } while (attacks < GameConfig.BERSERKER_MAX_CHAIN_ATTACKS &&
                CombatCalculator.checkChance(GameConfig.BERSERKER_CHAIN_ATTACK_CHANCE));

        String action;
        String desc;

        if (attacks > 1) {
            action = "FRENZY";
            desc = enemy.getName() + " wpada w SZAŁ! Seria " + attacks + " ciosów [" + String.join(", ", details) + "]";
        } else {
            action = "ATTACK";
            desc = enemy.getName() + " wykonuje brutalny cios [" + details.get(0) + "]";
        }

        log.add(buildTurn(turn, enemy.getName(), action, totalDamage, Math.max(0, state.getPlayerHp()), desc));
    }

    private void enemyRuneAttack(Character player, Enemy enemy, CombatState state,
                                 String playerAbility, int turn, List<FightTurnDTO> log) {
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        boolean isCrit = CombatCalculator.isCriticalHit(enemy.getLuck());

        // Tropiciel Player vs Magia Enemy
        if (GameConfig.ABILITY_TROPICIEL.equals(playerAbility)) {
            int reducedDamage = CombatCalculator.applyMagicResist(damage);
            state.setPlayerHp(state.getPlayerHp() - reducedDamage);
            log.add(buildTurn(turn, enemy.getName(), "RUNE_RESIST", reducedDamage, Math.max(0, state.getPlayerHp()),
                    enemy.getName() + " rzuca runę, ale Twój amulet chroni! (" + damage + " -> " + reducedDamage + ")"));
            return;
        }

        if (isCrit) {
            damage = (int)(damage * 1.5);
            if (CombatCalculator.checkChance(GameConfig.MISTRZ_RUN_FREEZE_CHANCE)) {
                state.setPlayerIsFrozen(true);
                state.setPlayerHp(state.getPlayerHp() - damage);
                log.add(buildTurn(turn, enemy.getName(), "FREEZE", damage, Math.max(0, state.getPlayerHp()),
                        enemy.getName() + " rzuca lodową runę i Cię ZAMRAŻA!"));
                return;
            }
        }

        state.setPlayerHp(state.getPlayerHp() - damage);
        log.add(buildTurn(turn, enemy.getName(), "RUNE", damage, Math.max(0, state.getPlayerHp()),
                "Nieuchronna magia run uderza w Ciebie!"));
    }

    private void enemySkaldAttack(Character player, Enemy enemy, CombatState state, int enemyMaxHp,
                                  String playerAbility, int turn, List<FightTurnDTO> log) {
        int songRoll = CombatCalculator.rollSkaldSong();
        int damage = CombatCalculator.calculateEnemyDamage(enemy);
        String desc;
        int finalDamage;
        AttackResult result; // Deklaracja zmiennej dla wyniku

        switch (songRoll) {
            case 1 -> { // Odwaga
                int boostedDmg = CombatCalculator.applySkaldDamageBoost(damage);
                result = tryDefense(boostedDmg, false, playerAbility); // Prawdziwy atak
                finalDamage = result.damage;
                desc = enemy.getName() + " śpiewa PIEŚŃ ODWAGI! Atak: " + finalDamage;
                if (result.blocked && result.reflected > 0) state.setEnemyHp(state.getEnemyHp() - result.reflected);
            }
            case 2 -> { // Leczenie
                int heal = CombatCalculator.calculateSkaldHeal(enemyMaxHp);
                state.setEnemyHp(Math.min(enemyMaxHp, state.getEnemyHp() + heal));

                result = tryDefense(damage, false, playerAbility); // Prawdziwy atak
                finalDamage = result.damage;
                desc = enemy.getName() + " śpiewa PIEŚŃ TROLA i leczy się (" + heal + ")!";
                if (result.blocked && result.reflected > 0) state.setEnemyHp(state.getEnemyHp() - result.reflected);
            }
            default -> { // Fałszowanie
                int passiveDmg = CombatCalculator.calculateSkaldPassiveDamage(damage);
                result = tryDefense(damage, false, playerAbility); // Prawdziwy atak
                finalDamage = result.damage + passiveDmg;
                desc = enemy.getName() + " FAŁSZUJE! Pasywne dmg + atak.";
                if (result.blocked && result.reflected > 0) state.setEnemyHp(state.getEnemyHp() - result.reflected);
            }
        }

        // TERAZ POPRAWNIE SPRAWDZAMY CZY UNIKAMY (używając wyniku prawdziwego ataku 'result')
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

        // SPRAWDZENIE ZAABSORBOWANYCH OBRAŻEŃ WROGA
        if (state.getEnemyAbsorbedDamage() > 0) {
            damage += state.getEnemyAbsorbedDamage();  // Dodaj zaabsorbowane obrażenia
            descPrefix = "[PIKOWANIE +" + state.getEnemyAbsorbedDamage() + " dmg] ";
            state.setEnemyAbsorbedDamage(0); // Reset zaabsorbowanych obrażeń
            action = "DIVE";
        } else if (isCrit) {
            damage = (int)(damage * 1.5);
        }

        AttackResult result = tryDefense(damage, isCrit || "DIVE".equals(action), playerAbility);

        if (result.blocked && result.reflected > 0) {
            state.setEnemyHp(state.getEnemyHp() - result.reflected);
            descPrefix += "(Odbito " + result.reflected + ") ";
        }
        if (result.dodged && result.givesDiveBonus) {
            int absorbed = (result.originalDamage * GameConfig.WALKIRIA_DAMAGE_ABSORPTION_PERCENT) / 100;
            state.setPlayerAbsorbedDamage(state.getPlayerAbsorbedDamage() + absorbed);
        }

        state.setPlayerHp(state.getPlayerHp() - result.damage);
        logAttack(turn, enemy.getName(), player.getName(), result, isCrit, state.getPlayerHp(), log, descPrefix);
    }

    // ========================================================
    // SYSTEM OBRONY
    // ========================================================

    private AttackResult tryDefense(int damage, boolean isCrit, String defenderAbility) {
        AttackResult result = new AttackResult();
        result.damage = damage;
        result.originalDamage = damage;  // Zapisz oryginalne obrażenia

        if (defenderAbility == null) return result;

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
    // METODY POMOCNICZE
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
        log.add(buildTurn(turn, name, "FROZEN", 0, hp, name + " jest zamrożony i traci turę!"));
    }

    private void logAttack(int turn, String attacker, String target, AttackResult result, boolean isCrit, int targetHp, List<FightTurnDTO> log, String prefix) {
        String action = "ATTACK";
        String desc = prefix + attacker + " atakuje.";

        if (result.blocked) {
            action = "BLOCKED";
            desc = prefix + target + " BLOKUJE atak!";
            if (result.reflected > 0) desc += " (Odbicie!)";
        } else if (result.dodged) {
            action = "DODGED";
            desc = prefix + target + " wykonuje UNIK!";
        } else {
            if (isCrit) {
                action = "CRITICAL";
                desc = prefix + attacker + " zadaje KRYTYCZNE obrażenia! (" + result.damage + ")";
            } else {
                desc = prefix + attacker + " trafia za " + result.damage + ".";
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

    // Klasa wewnętrzna tylko do obsługi wyniku obrony
    private static class AttackResult {
        int damage = 0;
        int originalDamage = 0;  // Oryginalne obrażenia przed obroną (do absorbcji Walkirii)
        boolean blocked = false;
        boolean dodged = false;
        boolean givesDiveBonus = false;
        int reflected = 0;
    }

    public List<EnemyDTO> getEnemiesAroundLevel(int playerLevel) {
        // Logika biznesowa obliczania zakresu (np. +/- 2 level)
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
                .map(EnemyDTO::fromEntity) // Używamy naszej metody mapującej
                .collect(Collectors.toList());
    }
}