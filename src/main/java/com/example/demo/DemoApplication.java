package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.model.Character;
import com.example.demo.model.CharacterItem;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner demo(
            CharacterClassRepository characterClassRepository,
            QuestRepository questRepository,
            ItemRepository itemRepository,
            UserRepository userRepository,
            CharacterRepository characterRepository,
            StatsRepository statsRepository,
            CharacterItemRepository characterItemRepository,
            PasswordEncoder passwordEncoder // Potrzebne do stworzenia usera
    ) {
        return (args) -> {
            // 1. Dodawanie Klas Postaci
            if (characterClassRepository.count() == 0) {
                characterClassRepository.save(new CharacterClass("Warrior", "Strength"));
                characterClassRepository.save(new CharacterClass("Mage", "Intelligence"));
                characterClassRepository.save(new CharacterClass("Scout", "Dexterity"));
                System.out.println("Default character classes added.");
            }

            // 2. Dodawanie Questów
            if (questRepository.count() == 0) {
                questRepository.save(new Quest(null, "Szczury w piwnicy", "Karczmarz narzeka na hałas.", 5, 20, 100));
                questRepository.save(new Quest(null, "Zaginiony Pierścień", "Ktoś zgubił sygnet w lesie.", 10, 50, 50));
                questRepository.save(new Quest(null, "Goblin Złodziej", "Mały zielony stwór kradnie kury.", 15, 80, 120));
                questRepository.save(new Quest(null, "Wilki na trakcie", "Kupcy boją się przejeżdżać.", 20, 120, 100));
                questRepository.save(new Quest( null, "Stary Cmentarz", "Dziwne dźwięki dochodzą z grobów.", 30, 200, 50));
                System.out.println("Questy zostały dodane do bazy.");
            }

            // 3. Dodawanie Przedmiotów (Items)
            if (itemRepository.count() == 0) {
                // Konstruktor: Name, Type, Price, Str, Int, Dex, Con, Luck
                Item sword = new Item("Wooden Sword", ItemType.WEAPON, 10, 5, 0, 0, 0, 0);
                Item armor = new Item("Leather Armor", ItemType.ARMOR, 20, 0, 0, 0, 5, 0);

                itemRepository.save(sword);
                itemRepository.save(armor);
                System.out.println("Default items added.");
            }

            // 4. Dodawanie Użytkownika i Postaci (tylko jeśli nie ma userów)
            if (userRepository.count() == 0) {
                // A. Tworzymy Usera
                User user = new User();
                user.setUsername("test");
                user.setPasswordHash(passwordEncoder.encode("password"));
                userRepository.save(user);

                // B. Tworzymy Statystyki
                Stats stats = new Stats(10, 10, 10, 10, 10);

                // C. Pobieramy klasę (np. Warrior)
                CharacterClass warriorClass = characterClassRepository.findByName("Warrior")
                        .orElseThrow(() -> new RuntimeException("Class not found"));

                // D. Tworzymy Postać
                Character character = new Character();
                character.setName("TestHero");
                character.setUser(user);
                character.setStats(stats);
                character.setCharacterClass(warriorClass);
                character.setLevel(1);
                character.setExperience(0);
                character.setGold(100);

                characterRepository.save(character);
                System.out.println("Test User and Character created.");

                // 5. Dodawanie Przedmiotu do Ekwipunku Postaci
                // Pobieramy miecz, który dodaliśmy w kroku 3
                Item sword = itemRepository.findAll().stream()
                        .filter(i -> i.getName().equals("Wooden Sword"))
                        .findFirst()
                        .orElseThrow();

                CharacterItem inventoryItem = new CharacterItem(character, sword);
                inventoryItem.setEquipped(true); // Od razu zakładamy miecz!

                characterItemRepository.save(inventoryItem);
                System.out.println("Sword added to inventory and equipped.");
            }
        };
    }
}