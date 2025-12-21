package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "character_classes")
public class CharacterClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String primaryStat;

    @Column(nullable = false)
    private String specialAbility;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "characterClass")
    @JsonIgnore
    private Set<Character> characters;

    public CharacterClass(String name, String primaryStat, String specialAbility, String description) {
        this.name = name;
        this.primaryStat = primaryStat;
        this.specialAbility = specialAbility;
        this.description = description;
    }

    public CharacterClass(String name, String primaryStat) {
        this.name = name;
        this.primaryStat = primaryStat;
        this.specialAbility = "NONE";
        this.description = "";

    }
}