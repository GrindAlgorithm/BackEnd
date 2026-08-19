package com.example.springboot.discussion.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토론 글 분류 — 연동 문서 §2.14. 두 축만: code_review / solution.
 * DB에는 enum 이름(대문자), JSON에는 소문자 value 로 노출.
 */
@Getter
@RequiredArgsConstructor
public enum DiscussionCategory {
    CODE_REVIEW("code_review"),
    SOLUTION("solution");

    @JsonValue
    private final String value;
}
