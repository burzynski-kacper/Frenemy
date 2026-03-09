package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.model.Character;
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
                        RaceRepository raceRepository,
                        QuestRepository questRepository,
                        ItemRepository itemRepository,
                        UserRepository userRepository,
                        CharacterRepository characterRepository,
                        StatsRepository statsRepository,
                        CharacterItemRepository characterItemRepository,
                        EnemyRepository enemyRepository,
                        PasswordEncoder passwordEncoder) {
                return (args) -> {
                        // 1. Dodawanie Ras
                        if (raceRepository.count() == 0) {
                                raceRepository.save(new Race(
                                                "Nordowie",
                                                "Ludzie Północy - zbalansowani wojownicy",
                                                0, 0, 0, 0, 0 // Brak bonusów/kar
                                ));
                                raceRepository.save(new Race(
                                                "Jotunowie",
                                                "Lodowe Giganty - potężni ale powolni",
                                                0, 0, -3, 5, 0 // +5 CON, -3 DEX
                                ));
                                raceRepository.save(new Race(
                                                "Svartalfar",
                                                "Mroczne Elfy - mistrzowie magii",
                                                -3, 5, 0, 0, 0 // +5 INT, -3 STR
                                ));
                                raceRepository.save(new Race(
                                                "Dvergar",
                                                "Krasnoludy - twardzi rzemieślnicy",
                                                2, 0, -3, 0, 3 // +2 STR, +3 LUCK, -3 DEX
                                ));
                                System.out.println("Rasy wikingów zostały dodane.");
                        }

                        // 2. Dodawanie Klas Postaci (Wikingie)
                        if (characterClassRepository.count() == 0) {
                                characterClassRepository.save(new CharacterClass(
                                                "Huskarl",
                                                "Strength",
                                                "BLOCK",
                                                "Elitarny strażnik Jarla. 25% szansa na blok, odbija 10% obrażeń przy zablokowanym krytyku. Jego tarcza służy też jako stół biesiadny."));
                                characterClassRepository.save(new CharacterClass(
                                                "Berserker",
                                                "Strength",
                                                "FRENZY",
                                                "Szalony wojownik w szale bitewnym. 50% szansa na kolejny atak (max 6), ale otrzymuje +10% obrażeń. Nie czuje bólu, dopóki nie wytrzeźwieje."));
                                characterClassRepository.save(new CharacterClass(
                                                "Tropiciel",
                                                "Dexterity",
                                                "DODGE",
                                                "Wilczy łowca z lasu. 50% szansa na unik, 20% odporności na magię. Twierdzi, że kąpiel raz w roku to strategia maskująca zapach."));
                                characterClassRepository.save(new CharacterClass(
                                                "Walkiria",
                                                "Dexterity",
                                                "DODGE_WALKIRIA",
                                                "Wysłanniczka Walhalli. 40% unik, po którym wykonuje Pikowanie (absorbuje 50% obrażeń). Często myli żywych z martwymi."));
                                characterClassRepository.save(new CharacterClass(
                                                "Mistrz Run",
                                                "Intelligence",
                                                "TRUE_STRIKE",
                                                "Zrzędliwy mędrzec z kamieniami runicznymi. Ataki zawsze trafiają, krytyk może zamrozić wroga. Nikt nie wie, czy czaruje, czy tylko rzuca kamieniami."));
                                characterClassRepository.save(new CharacterClass(
                                                "Skald",
                                                "Intelligence",
                                                "SHAPESHIFT",
                                                "Pieśniarz, którego głos rani uszy. Co turę inna pieśń: Odwaga (+dmg), Troll (heal), Fałszowanie (pasywne dmg). Wygnany za kwaśniejące mleko."));
                                System.out.println("Klasy wikingów zostały dodane.");
                        }

                        // 4. Dodawanie Przedmiotów (Items)
                        if (itemRepository.count() == 0) {
                                itemRepository.save(new Item("Żelazny Topór", ItemType.WEAPON, 50, 8, 0, 0, 0, 0));
                                itemRepository.save(new Item("Kolczuga", ItemType.ARMOR, 100, 0, 0, 0, 8, 0));
                                itemRepository.save(new Item("Amulet Odyna", ItemType.ACCESSORY, 75, 0, 5, 0, 0, 3));
                                System.out.println("Przedmioty wikingów zostały dodane.");
                        }

                        // 5. Tworzenie testowego użytkownika
                        if (userRepository.count() == 0) {
                                User user = new User();
                                user.setUsername("ragnar");
                                user.setPasswordHash(passwordEncoder.encode("vikings123"));
                                userRepository.save(user);

                                Stats stats = new Stats(12, 10, 10, 8, 10);

                                CharacterClass huskarl = characterClassRepository.findByName("Huskarl")
                                                .orElseThrow(() -> new RuntimeException("Klasa nie znaleziona"));

                                Race nord = raceRepository.findByName("Nordowie")
                                                .orElseThrow(() -> new RuntimeException("Rasa nie znaleziona"));

                                Character character = new Character();
                                character.setName("Ragnar Lothbrok");
                                character.setUser(user);
                                character.setStats(stats);
                                character.setCharacterClass(huskarl);
                                character.setRace(nord);
                                character.setLevel(1);
                                character.setExperience(0);
                                character.setGold(100);

                                characterRepository.save(character);
                                System.out.println("Testowy wiking Ragnar został stworzony.");

                                // Dodaj topór do ekwipunku
                                Item axe = itemRepository.findAll().stream()
                                                .filter(i -> i.getName().equals("Żelazny Topór"))
                                                .findFirst()
                                                .orElseThrow();

                                CharacterItem equippedAxe = new CharacterItem(character, axe);
                                equippedAxe.setEquipped(true);
                                characterItemRepository.save(equippedAxe);
                                System.out.println("Żelazny Topór dodany do ekwipunku.");
                        }

                        // 6. Dodawanie Przeciwników
                        if (enemyRepository.count() == 0) {
                                // Pobierz klasy
                                CharacterClass huskarl = characterClassRepository.findByName("Huskarl").orElse(null);
                                CharacterClass berserker = characterClassRepository.findByName("Berserker")
                                                .orElse(null);
                                CharacterClass tropiciel = characterClassRepository.findByName("Tropiciel")
                                                .orElse(null);
                                CharacterClass walkiria = characterClassRepository.findByName("Walkiria").orElse(null);
                                CharacterClass mistrzRun = characterClassRepository.findByName("Mistrz Run")
                                                .orElse(null);
                                CharacterClass skald = characterClassRepository.findByName("Skald").orElse(null);

                                // Przeciwnicy bez klasy (zwierzęta)
                                enemyRepository.save(new Enemy(
                                                "Dziki Wilk", "Głodny wilk z północnych lasów",
                                                1, 8, 2, 10, 8, 3, 30, 15, "QUEST"));

                                // Przeciwnicy z klasami
                                enemyRepository.save(new Enemy(
                                                "Goblin Złodziej", "Zwinny mały stwór",
                                                2, 6, 4, 12, 6, 8, 50, 25, "QUEST", tropiciel));
                                enemyRepository.save(new Enemy(
                                                "Bandyta Berserker", "Szalony rabuś z toporami",
                                                3, 14, 3, 8, 10, 4, 80, 40, "QUEST", berserker));
                                enemyRepository.save(new Enemy(
                                                "Troll Jaskiniowy", "Ogromny i głupi, ale silny",
                                                5, 18, 3, 6, 20, 2, 120, 60, "DUNGEON", huskarl));
                                enemyRepository.save(new Enemy(
                                                "Draugr Wojownik", "Nieumarły wiking z tarczą",
                                                7, 14, 6, 10, 16, 5, 180, 90, "DUNGEON", huskarl));
                                enemyRepository.save(new Enemy(
                                                "Mroczna Walkiria", "Upadła wysłanniczka Walhalli",
                                                8, 12, 8, 16, 12, 10, 220, 110, "DUNGEON", walkiria));
                                enemyRepository.save(new Enemy(
                                                "Nekromanta", "Mistrz mrocznych run",
                                                9, 6, 20, 8, 10, 8, 280, 140, "DUNGEON", mistrzRun));
                                enemyRepository.save(new Enemy(
                                                "Jotuński Berserker", "Szalony lodowy gigant",
                                                10, 22, 4, 8, 25, 4, 300, 150, "BOSS", berserker));
                                enemyRepository.save(new Enemy(
                                                "Loki Oszust", "Bóg chaosu we własnej osobie",
                                                12, 10, 22, 14, 15, 20, 500, 250, "BOSS", skald));
                                System.out.println("Przeciwnicy z klasami zostali dodani.");

                                // 3. Dodawanie Questów
                                if (questRepository.count() == 0) {
                                        questRepository.save(new Quest(null, "Szczury w Medhall",
                                                        "Karczmarz w Medhall narzeka na hałas z piwnicy.", 1, 50, 20,
                                                        1L, 1, null));
                                        questRepository.save(new Quest(null, "Zaginiony Amulet Thora",
                                                        "Jarl zgubił rodzinny amulet w lesie.", 5, 80, 40, 2L, 1,
                                                        null));
                                        questRepository.save(new Quest(null, "Trolle na szlaku",
                                                        "Kupcy boją się podróżować przez góry.", 10, 150, 80, 3L, 4,
                                                        null));
                                        questRepository.save(new Quest(null, "Draugr w kurhanie",
                                                        "Nieumarli wojownicy straszą wioskę.", 15, 250, 120, 4L, 7,
                                                        null));
                                        questRepository.save(new Quest(null, "Łup z Lindisfarne",
                                                        "Wyprawa łupieżcza na odległe wybrzeża.", 20, 400, 200, 5L, 10,
                                                        null));
                                        System.out.println("Questy wikingów zostały dodane.");
                                }
                        }
                };
        }
}