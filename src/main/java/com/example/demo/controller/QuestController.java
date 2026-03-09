package com.example.demo.controller;

import com.example.demo.model.Character;
import com.example.demo.model.Quest;
import com.example.demo.service.CharacterService;
import com.example.demo.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;
    private final CharacterService characterService;

    @GetMapping("/available")
    public ResponseEntity<List<Quest>> getAvailableQuests(@RequestParam Long userId) {
        return ResponseEntity.ok(questService.getQuestsForUser(userId));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startQuest(@RequestParam Long userId, @RequestParam Long questId) {
        System.out.println("--- ATTEMPT TO START QUEST: User " + userId + " Quest " + questId + " ---");
        try {
            var characterDTO = characterService.startQuest(userId, questId);
            return ResponseEntity.ok(characterDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getClass().getName() + " - " + e.getMessage());
        }

    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeQuest(@RequestParam Long userId) {
        System.out.println(">>> REQUEST ARRIVED AT CONTROLLER! UserID: " + userId);
        try {
            var updatedCharacterDTO = characterService.completeQuest(userId);
            return ResponseEntity.ok(updatedCharacterDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}