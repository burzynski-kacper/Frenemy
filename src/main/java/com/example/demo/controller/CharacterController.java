package com.example.demo.controller;

import com.example.demo.dto.CharacterDTO;
import com.example.demo.model.Character;
import com.example.demo.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/character")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCharacterDetails(@PathVariable Long userId) {
        try {
            CharacterDTO characterDTO = characterService.getCharacterResponse(userId);
            return ResponseEntity.ok(characterDTO);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Character for userId: " + userId + " doesn't exist.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Server error occurred while getting character.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}/inventory")
    public ResponseEntity<?> getInventory(@PathVariable Long userId) {
        try {
            var inventory = characterService.getCharacterInventory(userId);
            return ResponseEntity.ok(inventory);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Character not found.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Server error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}