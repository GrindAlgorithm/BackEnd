package com.example.springboot.language.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지원 언어 (요건 24). code 는 API 계약 문자열(LanguageCode.value)과 1:1.
 * judge0_id 는 이 테이블이 단일 소유 — 채점/실행 시 enum 기본값 대신 여기 값을 쓴다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "language")
public class LanguageEntity {

    @Id
    @Column(length = 16)
    private String code; // java11 | python3 | cpp17 | nodejs ...

    @Column(nullable = false, length = 64)
    private String label;

    @Column(name = "judge0_id", nullable = false)
    private int judge0Id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
