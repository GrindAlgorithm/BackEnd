package com.example.springboot.season.service;

import com.example.springboot.problem.dto.ProblemSummaryDTO;
import com.example.springboot.season.dto.SeasonDTO;

import java.util.List;

public interface SeasonService {

    /** 시즌 목록 (문제 목록 화면 탭) — 연동 문서 §2.5 */
    public List<SeasonDTO> getSeasons();

    /** 특정 시즌의 문제 목록 — 연동 문서 §2.6 */
    public List<ProblemSummaryDTO> getSeasonProblems(Integer seasonId);
}
