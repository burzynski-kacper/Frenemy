package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@Table(name = "races")
@EqualsAndHashCode(exclude = {"characters"})
@ToString(exclude = {"characters"})
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    private int strengthBonus = 0;
    private int intelligenceBonus = 0;
    private int dexterityBonus = 0;
    private int constitutionBonus = 0;
    private int luckBonus = 0;

    public Race(String name, String description, int strBonus, int intBonus, int dexBonus, int conBonus, int luckBonus) {
        this.name = name;
        this.description = description;
        this.strengthBonus = strBonus;
        this.intelligenceBonus = intBonus;
        this.dexterityBonus = dexBonus;
        this.constitutionBonus = conBonus;
        this.luckBonus = luckBonus;
    }
}
