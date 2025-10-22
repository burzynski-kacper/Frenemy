package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int level = 1;
    private long experience = 0;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stats_id", referencedColumnName = "id")
    private Stats stats;

    @ManyToOne
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private CharacterClass characterClass;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
