
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "characters")
@EqualsAndHashCode(exclude = {"inventory", "stats", "user", "characterClass", "currentQuest", "race"})
@ToString(exclude = {"inventory", "stats", "user", "characterClass", "currentQuest", "race"})
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int level = 1;
    private long experience = 0;
    private LocalDateTime questEndTime;

    @Column(nullable = false)
    private long gold = 10;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stats_id", referencedColumnName = "id")
    private Stats stats;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private CharacterClass characterClass;

    @ManyToOne
    @JoinColumn(name = "race_id", referencedColumnName = "id")
    private Race race;

    @ManyToOne
    @JoinColumn(name = "current_quest_id")
    private Quest currentQuest;

    @OneToMany(mappedBy = "character")
    private Set<CharacterItem> inventory;

    public boolean isBusy() {
        return questEndTime != null && questEndTime.isAfter(LocalDateTime.now());
    }
}