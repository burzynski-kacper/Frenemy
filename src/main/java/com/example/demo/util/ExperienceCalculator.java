package com.example.demo.util;

import com.example.demo.config.GameConfig;

public final class ExperienceCalculator {

    private ExperienceCalculator() {
        // Klasa narzędziowa - brak instancji
    }

    /**
     * Oblicza ile XP potrzeba żeby osiągnąć dany level.
     * Formuła: 100 * level * (level + 1) / 2
     *
     * Level 2:  100 XP (suma: 100)
     * Level 3:  300 XP (suma: 400)
     * Level 4:  600 XP (suma: 1000)
     * Level 5:  1000 XP (suma: 2000)
     * ...
     */
    public static long getXpRequiredForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        return (long) GameConfig.BASE_XP_MULTIPLIER * level * (level + 1) / 2;
    }

    /**
     * Oblicza całkowite XP potrzebne od poziomu 1 do danego poziomu.
     */
    public static long getTotalXpForLevel(int level) {
        long totalXp = 0;
        for (int i = 2; i <= level; i++) {
            totalXp += getXpRequiredForLevel(i);
        }
        return totalXp;
    }

    /**
     * Oblicza jaki level powinna mieć postać z daną ilością XP.
     */
    public static int calculateLevelForXp(long totalXp) {
        int level = 1;
        long xpNeeded = 0;

        while (level < GameConfig.MAX_LEVEL) {
            long xpForNextLevel = getXpRequiredForLevel(level + 1);
            if (xpNeeded + xpForNextLevel > totalXp) {
                break;
            }
            xpNeeded += xpForNextLevel;
            level++;
        }

        return level;
    }

    /**
     * Sprawdza ile XP brakuje do następnego poziomu.
     */
    public static long getXpToNextLevel(int currentLevel, long currentXp) {
        if (currentLevel >= GameConfig.MAX_LEVEL) {
            return 0;
        }

        long xpForCurrentLevel = getTotalXpForLevel(currentLevel);
        long xpForNextLevel = getTotalXpForLevel(currentLevel + 1);
        long xpNeededForNext = xpForNextLevel - xpForCurrentLevel;
        long xpProgressInLevel = currentXp - xpForCurrentLevel;

        return xpNeededForNext - xpProgressInLevel;
    }

    /**
     * Zwraca progres procentowy do następnego poziomu (0-100).
     */
    public static int getXpProgressPercent(int currentLevel, long currentXp) {
        if (currentLevel >= GameConfig.MAX_LEVEL) {
            return 100;
        }

        long xpForCurrentLevel = getTotalXpForLevel(currentLevel);
        long xpForNextLevel = getTotalXpForLevel(currentLevel + 1);
        long xpNeededForNext = xpForNextLevel - xpForCurrentLevel;
        long xpProgressInLevel = currentXp - xpForCurrentLevel;

        return (int) (xpProgressInLevel * 100 / xpNeededForNext);
    }
}