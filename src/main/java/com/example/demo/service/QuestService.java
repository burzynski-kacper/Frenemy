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
        // 1. Pobierz level gracza
        Character character = characterRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Postać nie znaleziona"));

        int playerLevel = character.getLevel();

        // 2. Szukamy misji w zakresie +/- 2 levele (żeby nie było za łatwo ani za trudno)
        int min = Math.max(1, playerLevel - 2);
        int max = playerLevel + 2;

        List<Quest> availableQuests = questRepository.findQuestsByLevelRange(min, max);

        // Zabezpieczenie: Jeśli nie ma misji na ten level, pobierz jakiekolwiek (np. dla levelu 1)
        // żeby Karczma nie była pusta.
        if (availableQuests.isEmpty()) {
            availableQuests = questRepository.findQuestsByLevelRange(playerLevel, playerLevel + 10);
        }

        // 3. Wymieszaj i weź 3
        Collections.shuffle(availableQuests);
        return availableQuests.stream()
                .limit(3)
                .collect(Collectors.toList());
    }
}