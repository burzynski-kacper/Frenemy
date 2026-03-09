package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "enemies")
public class Enemy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private int level;

    private int strength;
    private int intelligence;
    private int dexterity;
    private int constitution;
    private int luck;

    private int rewardXp;
    private int rewardGold;

    @Column(nullable = false)
    private String enemyType; // QUEST, DUNGEON, BOSS

    @ManyToOne
    @JoinColumn(name = "class_id")
    private CharacterClass characterClass;

    public Enemy(String name, String description, int level,
            int str, int intel, int dex, int con, int luck,
            int rewardXp, int rewardGold, String enemyType) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.strength = str;
        this.intelligence = intel;
        this.dexterity = dex;
        this.constitution = con;
        this.luck = luck;
        this.rewardXp = rewardXp;
        this.rewardGold = rewardGold;
        this.enemyType = enemyType;
    }

    public Enemy(String name, String description, int level,
            int str, int intel, int dex, int con, int luck,
            int rewardXp, int rewardGold, String enemyType, CharacterClass characterClass) {
        this(name, description, level, str, intel, dex, con, luck, rewardXp, rewardGold, enemyType);
        this.characterClass = characterClass;
    }

    public int getMaxHp() {
        return constitution * 10 + level * 5;
    }

    /**
     * Returns the enemy's special ability (or null if no class).
     */
    public String getAbility() {
        return characterClass != null ? characterClass.getSpecialAbility() : null;
    }
}