package com.example.demo.service;

import com.example.demo.dto.CharacterDTO;
import com.example.demo.model.*;
import com.example.demo.model.Character;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final QuestRepository questRepository;

    public CharacterDTO getCharacterResponse(Long userId) {
        Character character = getCharacterByUserId(userId);

        if (character.getCharacterClass() == null) {
            throw new IllegalStateException("Postać nie ma przypisanej klasy!");
        }
        if (character.getUser() == null) {
            throw new IllegalStateException("Postać nie ma przypisanego użytkownika!");
        }


        Stats calculateStats = calculateTotalStats(character);

        return CharacterDTO.builder()
                .id(character.getId())
                .name(character.getName())
                .level(character.getLevel())
                .experience(character.getExperience())
                .gold(character.getGold())
                .className(character.getCharacterClass().getName())
                .username(character.getUser().getUsername())
                .stats(calculateStats)
                .build();
    }

    @Transactional
    public CharacterDTO startQuest(Long userId, Long questId) {
        Character character = getCharacterByUserId(userId);

        if (character.isBusy()) {
            throw new IllegalStateException("Postać jest już na misji! Musisz poczekać.");
        }

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono takiego questu."));

        character.setCurrentQuest(quest);
        character.setQuestEndTime(LocalDateTime.now().plusMinutes(quest.getDurationMinutes()));

        characterRepository.save(character);

        return getCharacterResponse(userId);
    }


    @Transactional
    public CharacterDTO completeQuest(Long userId) {
        Character character = getCharacterByUserId(userId);

        if (character.getCurrentQuest() == null || character.getQuestEndTime() == null) {
            throw new IllegalStateException("Postać nie jest aktualnie na żadnej misji.");
        }

        if (LocalDateTime.now().isBefore(character.getQuestEndTime())) {
            throw new IllegalStateException("Misja jeszcze trwa! Wróć później.");
        }

        Quest finishedQuest = character.getCurrentQuest();
        int goldReward = finishedQuest.getRewardGold();
        int expReward = finishedQuest.getRewardXp();

        character.setGold(character.getGold() + goldReward);
        character.setExperience(character.getExperience() + expReward);

        character.setCurrentQuest(null);
        character.setQuestEndTime(null);

        Character savedCharacter = characterRepository.save(character);

        return getCharacterResponse(savedCharacter.getUser().getId());
    }

    private Character getCharacterByUserId(Long userId) {
        return characterRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Can't find character for this userId."));
    }

    private Stats calculateTotalStats(Character character) {
        Stats base = character.getStats();

        if (base == null) {
            throw new IllegalStateException("Postać nie ma przypisanych statystyk!");
        }


        int totalStrength = base.getStrength();
        int totalIntelligence = base.getIntelligence();
        int totalDexterity = base.getDexterity();
        int totalConstitution = base.getConstitution();
        int totalLuck = base.getLuck();

        if (character.getInventory() != null && !character.getInventory().isEmpty()) {
            List<CharacterItem> inventoryCopy = new ArrayList<>(character.getInventory());
            for (CharacterItem itemEntry : inventoryCopy) {
                if (itemEntry.isEquipped()) {
                    Item item = itemEntry.getItem();
                    totalStrength += item.getStrengthBonus();
                    totalIntelligence += item.getIntelligenceBonus();
                    totalDexterity += item.getDexterityBonus();
                    totalConstitution += item.getConstitutionBonus();
                    totalLuck += item.getLuckBonus();
                }
            }
        }

        return new Stats(totalStrength, totalDexterity, totalConstitution, totalLuck, totalIntelligence);
    }
}