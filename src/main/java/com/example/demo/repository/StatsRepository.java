package com.example.demo.repository;

import com.example.demo.model.Stats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<Stats, Long> { }