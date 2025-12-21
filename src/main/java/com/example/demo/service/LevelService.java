package com.example.demo.service;

import com.example.demo.config.GameConfig;
import com.example.demo.model.Character;
import com.example.demo.model.Stats;
import com.example.demo.util.ExperienceCalculator;
import org.springframework.stereotype.Service;

@Service
public class LevelService {

    /**
     * Sprawdza i wykonuje awans postaci jeśli ma wystarczająco XP.
     * Może awansować o kilka poziomów naraz.
     *
     * @return liczba zdobytych poziomów (0 jeśli brak awansu)
     */
    public int checkAndLevelUp(Character character) {
        int levelsGained = 0;

        while (canLevelUp(character)) {
            levelUp(character);
            levelsGained++;
        }

        return levelsGained;
    }

    /**
     * Sprawdza czy postać może awansować.
     */
    public boolean canLevelUp(Character character) {
        if (character.getLevel() >= GameConfig.MAX_LEVEL) {
            return false;
        }

        int expectedLevel = ExperienceCalculator.calculateLevelForXp(character.getExperience());
        return expectedLevel > character.getLevel();
    }

    /**
     * Awansuje postać o jeden poziom i przydziela punkty statystyk.
     */
    private void levelUp(Character character) {
        character.setLevel(character.getLevel() + 1);

        // Dodaj punkty statystyk
        Stats stats = character.getStats();
        if (stats != null) {
            distributeStatPoints(stats);
        }
    }

    /**
     * Rozdziela punkty statystyk przy awansie.
     * Domyślnie: +1 do każdej statystyki.
     */
    private void distributeStatPoints(Stats stats) {
        // 5 punktów = +1 do każdej statystyki
        stats.setStrength(stats.getStrength() + 1);
        stats.setDexterity(stats.getDexterity() + 1);
        stats.setConstitution(stats.getConstitution() + 1);
        stats.setIntelligence(stats.getIntelligence() + 1);
        stats.setLuck(stats.getLuck() + 1);
    }
}