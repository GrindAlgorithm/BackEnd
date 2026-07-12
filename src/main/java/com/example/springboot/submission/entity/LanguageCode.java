package com.example.springboot.submission.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 제출 언어 코드 — 연동 문서 §1.6 / §3.1.
 * judge0Id 는 Judge0 language_id (CE 기준) — 백엔드 내부에서만 사용, API 계약엔 value 문자열만 노출.
 * self-host 빌드에 따라 id가 달라질 수 있어 이 매핑만 조정하면 된다.
 */
@Getter
@RequiredArgsConstructor
public enum LanguageCode {
    JAVA11("java11", 62),   // OpenJDK
    PYTHON3("python3", 71), // Python 3.8
    CPP17("cpp17", 54),     // GCC, -std=c++17
    NODEJS("nodejs", 63);   // Node.js

    @JsonValue
    private final String value;

    private final int judge0Id;

    /** 요청 본문의 "java11" 등 문자열 → enum (연동 문서 §1.6) */
    @JsonCreator
    public static LanguageCode fromValue(String value) {
        return Arrays.stream(values())
                .filter(l -> l.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown language: " + value));
    }
}
