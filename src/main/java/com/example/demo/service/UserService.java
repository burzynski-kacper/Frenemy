package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.CharacterClass;
import com.example.demo.model.Race;
import com.example.demo.model.Stats;
import com.example.demo.model.User;
import com.example.demo.model.Character;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CharacterRepository characterRepository;
    private final StatsRepository statsRepository;
    private final CharacterClassRepository characterClassRepository;
    private final RaceRepository raceRepository;

    public User registerNewUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ta nazwa użytkownika jest już zajęta!");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        CharacterClass selectedClass = characterClassRepository.findByName(request.getClassName())
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowa klasa: " + request.getClassName()));

        Race selectedRace = raceRepository.findByName(request.getRaceName())
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowa rasa: " + request.getRaceName()));

        // Bazowe statystyki + bonusy rasowe
        Stats initialStats = new Stats(
                10 + selectedRace.getStrengthBonus(),
                10 + selectedRace.getDexterityBonus(),
                10 + selectedRace.getConstitutionBonus(),
                10 + selectedRace.getLuckBonus(),
                10 + selectedRace.getIntelligenceBonus()
        );
        Stats savedStats = statsRepository.save(initialStats);

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(hashedPassword);
        User savedUser = userRepository.save(newUser);

        Character newCharacter = new Character();
        newCharacter.setName(request.getCharacterName() != null ? request.getCharacterName() : savedUser.getUsername());
        newCharacter.setUser(savedUser);
        newCharacter.setStats(savedStats);
        newCharacter.setCharacterClass(selectedClass);
        newCharacter.setRace(selectedRace);

        characterRepository.save(newCharacter);

        return savedUser;
    }
}