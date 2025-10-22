package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "stats")
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int strength;

    @Column(nullable = false)
    private int intelligence;

    @Column(nullable = false)
    private int dexterity;

    @Column(nullable = false)
    private int constitution;

    @Column(nullable = false)
    private int luck;

    public Stats(int strength, int dexterity, int constitution, int luck, int intelligence) {
        this.strength = strength;
        this.intelligence = intelligence;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.luck = luck;
    }
}