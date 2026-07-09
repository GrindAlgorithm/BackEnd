package com.example.springboot.problem.repository;

import com.example.springboot.problem.entity.SolveSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolveSessionRepository extends JpaRepository<SolveSessionEntity, String> {
}
