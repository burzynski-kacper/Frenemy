package com.example.demo.service;

import com.example.demo.config.GameConfig;
import com.example.demo.model.Character;
import com.example.demo.model.Stats;
import com.example.demo.util.ExperienceCalculator;
import org.springframework.stereotype.Service;

@Service
public class LevelService {

    /**
     * Checks and executes level up if the character has enough XP.
     * Can level up multiple levels at once.
     *
     * @return number of levels gained (0 if no level up)
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
     * Checks if the character can level up.
     */
    public boolean canLevelUp(Character character) {
        if (character.getLevel() >= GameConfig.MAX_LEVEL) {
            return false;
        }

        int expectedLevel = ExperienceCalculator.calculateLevelForXp(character.getExperience());
        return expectedLevel > character.getLevel();
    }

    /**
     * Levels up the character and distributes stat points.
     */
    private void levelUp(Character character) {
        character.setLevel(character.getLevel() + 1);

        // Add stat points
        Stats stats = character.getStats();
        if (stats != null) {
            distributeStatPoints(stats);
        }
    }

    /**
     * Distributes stat points when leveling up.
     * Default: +1 to each stat.
     */
    private void distributeStatPoints(Stats stats) {
        // 5 points = +1 to each stat
        stats.setStrength(stats.getStrength() + 1);
        stats.setDexterity(stats.getDexterity() + 1);
        stats.setConstitution(stats.getConstitution() + 1);
        stats.setIntelligence(stats.getIntelligence() + 1);
        stats.setLuck(stats.getLuck() + 1);
    }
}