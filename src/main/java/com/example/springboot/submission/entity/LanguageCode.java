package com.example.springboot.submission.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 제출 언어 코드 — 연동 문서 §1.6 / §3.1. Judge0 language_id 매핑은 백엔드 내부에서만.
 */
@Getter
@RequiredArgsConstructor
public enum LanguageCode {
    JAVA11("java11"),
    PYTHON3("python3"),
    CPP17("cpp17"),
    NODEJS("nodejs");

    @JsonValue
    private final String value;
}
