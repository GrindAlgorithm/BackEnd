package com.example.springboot.discussion.service;

import com.example.springboot.discussion.dto.DiscussionCreateRequestDTO;
import com.example.springboot.discussion.dto.DiscussionPostDetailDTO;
import com.example.springboot.discussion.dto.DiscussionResponseDTO;

public interface DiscussionService {

    /** 문제 토론 조회 — 정답자만 글 목록 접근(연동 문서 §2.14). */
    DiscussionResponseDTO getDiscussion(String problemId);

    /** 토론 글 상세 (본문 포함) — 정답자만 접근 (요건 4). */
    DiscussionPostDetailDTO getPost(String problemId, long postId);

    /** 토론 글 작성 — 정답자만 가능 (요건 4). 작성자 티어는 현재 시즌 랭킹 스냅샷. */
    DiscussionPostDetailDTO createPost(String problemId, DiscussionCreateRequestDTO request);
}
