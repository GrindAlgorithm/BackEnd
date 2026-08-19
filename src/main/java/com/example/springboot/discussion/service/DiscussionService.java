package com.example.springboot.discussion.service;

import com.example.springboot.discussion.dto.DiscussionResponseDTO;

public interface DiscussionService {

    /** 문제 토론 조회 — 정답자만 글 목록 접근(연동 문서 §2.14). */
    DiscussionResponseDTO getDiscussion(String problemId);
}
