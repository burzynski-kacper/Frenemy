package com.example.demo.service;

import com.example.demo.model.Quest;
import com.example.demo.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;

    public List<Quest> getThreeRandomQuests() {
        List<Quest> allQuests = questRepository.findAll();
        if (allQuests.size() <= 3) {
            return allQuests;
        }
        Collections.shuffle(allQuests);
        return allQuests.stream()
                .limit(3)
                .collect(Collectors.toList());
    }
}