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
            throw new IllegalStateException("Character has no class!");
        }
        if (character.getUser() == null) {
            throw new IllegalStateException("Character has no user  !");
        }

        Stats calculateStats = calculateTotalStats(character);

        return CharacterDTO.builder()
                .id(character.getId())
                .name(character.getName())
                .level(character.getLevel())
                .experience(character.getExperience())
                .gold(character.getGold())
                .className(character.getCharacterClass().getName())
                .raceName(character.getRace() != null ? character.getRace().getName() : "Unknown")
                .username(character.getUser().getUsername())
                .stats(calculateStats)
                .xpToNextLevel(ExperienceCalculator.getXpToNextLevel(character.getLevel(), character.getExperience()))
                .xpProgressPercent(
                        ExperienceCalculator.getXpProgressPercent(character.getLevel(), character.getExperience()))
                .build();
    }

    @Transactional
    public CharacterDTO startQuest(Long userId, Long questId) {
        Character character = getCharacterByUserId(userId);

        if (character.isBusy()) {
            throw new IllegalStateException("Character is already on a quest! You have to wait.");
        }

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found!"));

        // Walidacja poziomu
        if (character.getLevel() < quest.getMinLevel()) {
            throw new IllegalStateException("Your level is too low! Required: " + quest.getMinLevel());
        }

        character.setCurrentQuest(quest);
        character.setQuestEndTime(LocalDateTime.now().plusMinutes(quest.getDurationMinutes()));

        characterRepository.save(character);

        return getCharacterResponse(userId);
    }

    @Transactional
    public FightResultDTO completeQuest(Long userId) {
        Character character = characterRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Character doesn't exist!"));

        if (character.getCurrentQuest() == null) {
            throw new IllegalStateException("Character is not on a quest!");
        }

        Quest quest = questRepository.findById(character.getCurrentQuest().getId())
                .orElseThrow(() -> new IllegalStateException("Quest doesn't exist!"));

        // Validate quest time
        if (character.getQuestEndTime() != null && LocalDateTime.now().isBefore(character.getQuestEndTime())) {
            throw new IllegalStateException("Quest hasn't ended yet! Wait.");
        }

        // 1. FIGHT
        if (quest.getEnemyId() == null) {
            throw new IllegalStateException("Quest has no enemy!");
        }

        FightResultDTO result = combatService.fight(userId, quest.getEnemyId());

        // 2. IF WON -> GIVE REWARDS
        if (result.isPlayerWon()) {
            // Gold and XP from the quest
            character.setGold(character.getGold() + quest.getRewardGold());
            character.setExperience(character.getExperience() + quest.getRewardXp());

            // Check level up (if levelService is not called in combatService)
            levelService.checkAndLevelUp(character);

            // === ITEM LOGIC (Does this quest have an item?) ===
            if (quest.getRewardItemId() != null) {
                Item item = itemRepository.findById(quest.getRewardItemId())
                        .orElse(null);

                if (item != null) {
                    // Add to inventory
                    CharacterItem newItem = new CharacterItem();
                    newItem.setCharacter(character);
                    newItem.setItem(item);
                    newItem.setEquipped(false);
                    characterItemRepository.save(newItem);

                    System.out.println("Player received item: " + item.getName());
                }
            }
        }

        // 3. END QUEST (clear status)
        character.setCurrentQuest(null);
        character.setQuestEndTime(null);
        characterRepository.save(character);

        return result;
    }

    @Transactional
    public void equipItem(Long userId, Long itemId) {
        Character character = getCharacterByUserId(userId);

        CharacterItem characterItem = character.getInventory().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("You don't have this item!"));

        // Check if there is already an equipped item of the same type
        ItemType type = characterItem.getItem().getType();
        character.getInventory().stream()
                .filter(ci -> ci.isEquipped() && ci.getItem().getType() == type)
                .forEach(ci -> ci.setEquipped(false)); // Remove the old one

        characterItem.setEquipped(true);
    }

    @Transactional
    public void unequipItem(Long userId, Long itemId) {
        Character character = getCharacterByUserId(userId);

        CharacterItem characterItem = character.getInventory().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId) && ci.isEquipped())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("This item is not equipped!"));

        characterItem.setEquipped(false);
    }

    private Character getCharacterByUserId(Long userId) {
        return characterRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Can't find character for this userId."));
    }

    private Stats calculateTotalStats(Character character) {
        Stats base = character.getStats();

        if (base == null) {
            throw new IllegalStateException("Character has no stats!");
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

    public List<CharacterItem> getCharacterInventory(Long userId) {
        Character character = getCharacterByUserId(userId);
        return character.getInventory() != null
                ? new ArrayList<>(character.getInventory())
                : new ArrayList<>();
    }
}