package com.example.demo.controller;

import com.example.demo.model.Race;
import com.example.demo.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
public class RaceController {

    private final RaceRepository raceRepository;

    @GetMapping
    public ResponseEntity<List<Race>> getAllRaces() {
        return ResponseEntity.ok(raceRepository.findAll());
    }
}
