package com.example.demo.controller;

import com.example.demo.dto.FightResultDTO;
import com.example.demo.model.Enemy;
import com.example.demo.repository.EnemyRepository;
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
    private final EnemyRepository enemyRepository;

    @GetMapping("/enemies")
    public ResponseEntity<List<Enemy>> getAllEnemies() {
        return ResponseEntity.ok(enemyRepository.findAll());
    }

    @GetMapping("/enemies/level/{playerLevel}")
    public ResponseEntity<List<Enemy>> getEnemiesForLevel(@PathVariable int playerLevel) {
        return ResponseEntity.ok(enemyRepository.findEnemiesAroundLevel(playerLevel));
    }

    @PostMapping("/fight")
    public ResponseEntity<?> fight(@RequestParam Long userId, @RequestParam Long enemyId) {
        try {
            FightResultDTO result = combatService.fight(userId, enemyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd walki: " + e.getMessage());
        }
    }
}