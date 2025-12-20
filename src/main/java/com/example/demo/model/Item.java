package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private ItemType type;

    private int price;

    private int strengthBonus;
    private int intelligenceBonus;
    private int dexterityBonus;
    private int constitutionBonus;
    private int luckBonus;

    public Item(String name, ItemType type, int strengthBonus, int intelligenceBonus, int dexterityBonus, int constitutionBonus, int luckBonus, int price){
        this.name = name;
        this.type = type;
        this.price = price;
        this.strengthBonus = strengthBonus;
        this.intelligenceBonus = intelligenceBonus;
        this.dexterityBonus = dexterityBonus;
        this.constitutionBonus = constitutionBonus;
        this.luckBonus = luckBonus;
    }
}
