package com.example.springboot.integrity.repository;

import com.example.springboot.integrity.entity.SolveSessionSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolveSessionSummaryRepository extends JpaRepository<SolveSessionSummaryEntity, String> {
}
