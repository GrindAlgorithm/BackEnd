package com.example.springboot.ranking.service;

import com.example.springboot.ranking.dto.RankingDTO;
import com.example.springboot.ranking.dto.RankingScope;

public interface RankingService {

    /** 랭킹 조회 (season/overall/friends) — 연동 문서 §2.13 */
    public RankingDTO getRanking(RankingScope scope);
}
