package com.example.demo.repository;

import com.example.demo.model.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByUserId(Long userId);
}