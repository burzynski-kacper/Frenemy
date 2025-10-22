package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.CharacterClass;
import com.example.demo.model.Stats;
import com.example.demo.model.User;
import com.example.demo.model.Character;
import com.example.demo.repository.CharacterClassRepository;
import com.example.demo.repository.StatsRepository;
import com.example.demo.repository.CharacterRepository;
import com.example.demo.repository.UserRepository;
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

    public User registerNewUser(RegisterRequest request) {
        // Does user exist?
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("This username is taken!");
        }

        // Password hashing...
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Optional<CharacterClass> characterClassOpt = characterClassRepository.findByName(request.getClassName());
        if (characterClassOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid character class selected: " + request.getClassName());
        }
        CharacterClass selectedClass = characterClassOpt.get();

        // Creating and saving basic stats
        Stats initialStats = new Stats(10, 10, 10, 10, 10);
        Stats savedStats = statsRepository.save(initialStats);

        // Creating and saving a user
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(hashedPassword);
        User savedUser = userRepository.save(newUser);

        // Creating and saving a character
        Character newCharacter = new Character();
        newCharacter.setName(savedUser.getUsername());

        newCharacter.setUser(savedUser);
        newCharacter.setStats(savedStats);
        newCharacter.setCharacterClass(selectedClass);

        characterRepository.save(newCharacter);

        return savedUser;
    }
}
