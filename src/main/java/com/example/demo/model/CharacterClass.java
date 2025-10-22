package com.example.demo.model;

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

    @OneToMany(mappedBy = "characterClass")
    private Set<Character> characters;

    public CharacterClass(String name, String primaryStat) {
        this.name = name;
        this.primaryStat = primaryStat;
    }
}