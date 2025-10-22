package com.example.demo.repository;

import com.example.demo.model.CharacterClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterClassRepository extends JpaRepository<CharacterClass, Long> {
    Optional<CharacterClass> findByName(String name);
}