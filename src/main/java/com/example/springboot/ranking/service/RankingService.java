package com.example.springboot.ranking.service;

import com.example.springboot.ranking.dto.RankingDTO;
import com.example.springboot.ranking.dto.RankingEntryDTO;
import com.example.springboot.ranking.dto.RankingScope;

import java.util.List;

public interface RankingService {

    /** 랭킹 조회 (season/overall/friends) — 연동 문서 §2.13 */
    public RankingDTO getRanking(RankingScope scope);

    /** 현재 시즌 순위표 (점수 내림차순, rank 부여 완료). 진행 중인 시즌이 없으면 빈 목록 */
    public List<RankingEntryDTO> getCurrentSeasonEntries();

    /** 현재 시즌 내 순위 항목 — 미배치(첫 정답 전)거나 시즌이 없으면 null (GET /me 등) */
    public RankingEntryDTO getCurrentSeasonEntry(String handle);
}
