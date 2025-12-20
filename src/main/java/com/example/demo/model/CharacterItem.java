package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@Table(name = "character_items")
@EqualsAndHashCode(exclude = {"character", "item"})
@ToString(exclude = {"character", "item"})
public class CharacterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "character_id")
    private Character character;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private boolean isEquipped = false;

    public CharacterItem(Character character, Item item){
        this.character = character;
        this.item = item;
    }
}