package com.example.demo.service;

import com.example.demo.model.Quest;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.QuestRepository;
import com.example.demo.model.Character;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final CharacterRepository characterRepository;

    public List<Quest> getQuestsForUser(Long userId) {
        // 1. Get character level
        Character character = characterRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        int playerLevel = character.getLevel();

        // 2. Find quests in range +/- 2 levels (to make it neither too easy nor too
        // hard)
        int min = Math.max(1, playerLevel - 2);
        int max = playerLevel + 2;

        List<Quest> availableQuests = questRepository.findQuestsByLevelRange(min, max);

        // If no quests are available for this level, get any quests (e.g. for level 1)
        if (availableQuests.isEmpty()) {
            availableQuests = questRepository.findQuestsByLevelRange(playerLevel, playerLevel + 10);
        }

        // 3. Shuffle and return 3 of the available quests
        Collections.shuffle(availableQuests);
        return availableQuests.stream()
                .limit(3)
                .collect(Collectors.toList());
    }
}