package com.example.demo.dto;

import com.example.demo.model.Stats;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharacterDTO {
    private Long id;
    private String name;
    private int level;
    private long experience;
    private long gold;
    private String className;
    private String raceName;
    private String username;
    private Stats stats;
    private long xpToNextLevel;
    private int xpProgressPercent;
}