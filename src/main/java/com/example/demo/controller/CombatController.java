package com.example.demo.controller;

import com.example.demo.dto.EnemyDTO;
import com.example.demo.dto.FightResultDTO;
import com.example.demo.service.CombatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combat")
@RequiredArgsConstructor
public class CombatController {

    private final CombatService combatService;

    @GetMapping("/enemies")
    public ResponseEntity<List<EnemyDTO>> getAllEnemies() {
        return ResponseEntity.ok(combatService.getAllEnemies());
    }

    @GetMapping("/enemies/level/{playerLevel}")
    public ResponseEntity<List<EnemyDTO>> getEnemiesForLevel(@PathVariable int playerLevel) {
        return ResponseEntity.ok(combatService.getEnemiesAroundLevel(playerLevel));
    }

    @PostMapping("/fight")
    public ResponseEntity<?> fight(@RequestParam Long userId, @RequestParam Long enemyId) {
        try {
            FightResultDTO result = combatService.fight(userId, enemyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error during fight: " + e.getMessage());
        }
    }
}