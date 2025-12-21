package com.example.demo.dto;

import com.example.demo.model.Enemy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnemyDTO {
    private Long id;
    private String name;
    private String description;
    private int level;
    private int rewardXp;
    private int rewardGold;
    private String enemyType;

    private String className;
    private String specialAbility;

    private int strength;
    private int dexterity;
    private int intelligence;
    private int luck;
    private int constitution;
    private int hp;

    public static EnemyDTO fromEntity(Enemy enemy){
        return EnemyDTO.builder()
                .id(enemy.getId())
                .name(enemy.getName())
                .description(enemy.getDescription())
                .level(enemy.getLevel())
                .rewardXp(enemy.getRewardXp())
                .rewardGold(enemy.getRewardGold())
                .enemyType(enemy.getEnemyType())
                .strength(enemy.getStrength())
                .dexterity(enemy.getDexterity())
                .intelligence(enemy.getIntelligence())
                .constitution(enemy.getConstitution())
                .luck(enemy.getLuck())
                .hp(enemy.getMaxHp())
                .className(enemy.getCharacterClass() != null ? enemy.getCharacterClass().getName() : "Brak")
                .specialAbility(enemy.getCharacterClass() != null ? enemy.getCharacterClass().getSpecialAbility() : "Brak")
                .build();
    }
}
