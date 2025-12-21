package com.example.demo.service;

import com.example.demo.dto.CharacterDTO;
import com.example.demo.dto.FightResultDTO;
import com.example.demo.model.*;
import com.example.demo.model.Character;
import com.example.demo.repository.CharacterItemRepository;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.QuestRepository;
import com.example.demo.util.ExperienceCalculator;
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
    private final LevelService levelService;
    private final ItemRepository itemRepository;
    private final CharacterItemRepository characterItemRepository;
    private final CombatService combatService;


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
                .raceName(character.getRace() != null ? character.getRace().getName() : "Nieznana")
                .username(character.getUser().getUsername())
                .stats(calculateStats)
                .xpToNextLevel(ExperienceCalculator.getXpToNextLevel(character.getLevel(), character.getExperience()))
                .xpProgressPercent(ExperienceCalculator.getXpProgressPercent(character.getLevel(), character.getExperience()))
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
    public FightResultDTO completeQuest(Long userId) {
        Character character = characterRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Postać nie istnieje!"));

        if (character.getCurrentQuest() == null) {
            throw new IllegalStateException("Postać nie jest na misji!");
        }

        Quest quest = questRepository.findById(character.getCurrentQuest().getId())
                .orElseThrow(() -> new IllegalStateException("Misja nie istnieje!"));

        // 1. WALKA
        if (quest.getEnemyId() == null) {
            throw new IllegalStateException("Błąd danych: Misja bez przeciwnika!");
        }

        FightResultDTO result = combatService.fight(userId, quest.getEnemyId());

        // 2. JEŚLI WYGRANA -> ROZDAJ NAGRODY
        if (result.isPlayerWon()) {
            // Złoto i XP z definicji Misji
            character.setGold(character.getGold() + quest.getRewardGold());
            character.setExperience(character.getExperience() + quest.getRewardXp());

            // Sprawdzenie level up (jeśli levelService nie jest wywoływany w combatService)
            levelService.checkAndLevelUp(character);

            // === LOGIKA ITEMU (Czy ta misja ma przypisany przedmiot?) ===
            if (quest.getRewardItemId() != null) {
                Item item = itemRepository.findById(quest.getRewardItemId())
                        .orElse(null);

                if (item != null) {
                    // Dodaj do ekwipunku
                    CharacterItem newItem = new CharacterItem();
                    newItem.setCharacter(character);
                    newItem.setItem(item);
                    newItem.setEquipped(false);
                    characterItemRepository.save(newItem);

                    System.out.println("NAGRODA: Gracz otrzymał przedmiot: " + item.getName());
                }
            }
        }

        // 3. Koniec misji (czyścimy status)
        character.setCurrentQuest(null);
        character.setQuestEndTime(null);
        characterRepository.save(character);

        return result;
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