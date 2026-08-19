package com.example.springboot.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 연동 문서 §2.14 stats — 잠금 화면에도 노출되는 토론 통계. */
@Getter
@AllArgsConstructor
public class DiscussionStatsDTO {
    private long postCount;
    private long publicSolutionCount; // category=solution 글 수
    private long codeReviewCount;     // category=code_review 글 수
}
