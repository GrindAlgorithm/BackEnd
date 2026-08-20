package com.example.springboot.season.service;

import com.example.springboot.problem.dto.ProblemSummaryDTO;
import com.example.springboot.season.dto.SeasonDTO;
import com.example.springboot.season.dto.SeasonDetailDTO;

import java.util.List;

public interface SeasonService {

    /** 시즌 목록 (문제 목록 화면 탭) — 연동 문서 §2.5 */
    public List<SeasonDTO> getSeasons();

    /** 시즌 단건 기간 정보 (없으면 null) — 문제 탭 시즌 정보 */
    public SeasonDTO getSeason(Integer seasonId);

    /** 특정 시즌의 문제 목록 — 연동 문서 §2.6 */
    public List<ProblemSummaryDTO> getSeasonProblems(Integer seasonId);

    /** 시즌 화면 통합 응답 — 진행률/문제/리워드/지난 시즌 (현재 시즌 없으면 null) */
    public SeasonDetailDTO getCurrentSeasonDetail();
}
