package com.example.springboot.submission.service;

import com.example.springboot.submission.dto.SubmissionDTO;

import java.util.List;

public interface SubmissionService {

    /**
     * 채점 현황 목록 (최근순) — 연동 문서 §2.12.
     *
     * @param seasonId  시즌 필터 (null이면 전체)
     * @param problemId 문제 필터 (null이면 전체)
     * @param mine      내 제출만 (유저 도메인 연동 전까지는 미적용)
     */
    public List<SubmissionDTO> getSubmissions(Integer seasonId, String problemId, boolean mine);
}
