package com.example.springboot.submission.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 제출 채점 상태 — 연동 문서 §1.6 SubmissionStatus.
 * DB에는 enum 이름으로 저장되고, JSON에는 snake_case 값으로 노출된다.
 */
@Getter
@RequiredArgsConstructor
public enum SubmissionStatus {
    QUEUED("queued"),
    JUDGING("judging"),
    ACCEPTED("accepted"),
    WRONG_ANSWER("wrong_answer"),
    TIME_LIMIT("time_limit"),
    MEMORY_LIMIT("memory_limit"),
    RUNTIME_ERROR("runtime_error"),
    COMPILE_ERROR("compile_error");

    @JsonValue
    private final String value;
}
