package com.example.demo.controller;

import com.example.demo.model.CharacterClass;
import com.example.demo.repository.CharacterClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final CharacterClassRepository characterClassRepository;

    @GetMapping
    public ResponseEntity<List<CharacterClass>> getAllClasses() {
        return ResponseEntity.ok(characterClassRepository.findAll());
    }
}
