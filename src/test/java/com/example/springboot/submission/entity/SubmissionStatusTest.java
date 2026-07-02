package com.example.springboot.submission.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 채점 상태/언어 JSON 값 매핑 검증 (Spring/DB 불필요) — 연동 문서 §1.6 */
class SubmissionStatusTest {

    @Test
    void statusJsonValuesAreSnakeCase() {
        assertEquals("accepted", SubmissionStatus.ACCEPTED.getValue());
        assertEquals("wrong_answer", SubmissionStatus.WRONG_ANSWER.getValue());
        assertEquals("compile_error", SubmissionStatus.COMPILE_ERROR.getValue());
    }

    @Test
    void languageJsonValues() {
        assertEquals("java11", LanguageCode.JAVA11.getValue());
        assertEquals("cpp17", LanguageCode.CPP17.getValue());
        assertEquals("nodejs", LanguageCode.NODEJS.getValue());
    }
}
