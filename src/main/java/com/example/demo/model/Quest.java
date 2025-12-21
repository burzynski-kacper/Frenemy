package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int durationMinutes;
    private int rewardXp;
    private int rewardGold;
    private Long enemyId;
    private int minLevel;

    @Column(nullable = true)
    private Long rewardItemId;
}
