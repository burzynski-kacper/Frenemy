package com.example.demo.repository;

import com.example.demo.model.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    @Query("SELECT q FROM Quest q WHERE q.minLevel BETWEEN :min AND :max")
    List<Quest> findQuestsByLevelRange(@Param("min") int min, @Param("max") int max);
}
