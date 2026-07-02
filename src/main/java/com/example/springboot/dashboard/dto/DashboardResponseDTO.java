package com.example.springboot.dashboard.dto;

import com.example.springboot.notice.dto.NoticeResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** 홈 화면 통합 응답 — 연동 문서 §2.4 GET /dashboard (DashboardResponse) */
@Getter
@AllArgsConstructor
public class DashboardResponseDTO {
    private DecayWarningDTO decay;                    // 하락 공식 미확정 → null
    private List<TodayPickDTO> todayPicks;            // 오늘의 추천
    private List<NearbyRankingEntryDTO> nearbyRanking; // 내 주변 순위(±2)
    private SeasonProgressDTO season;                 // 시즌 정보
    private WeeklyStatsDTO weekly;                    // 이번주 통계
    private List<NoticeResponseDTO> notices;          // 공지 목록
    private ActivityCalendarDTO seasonActivity;       // 이번 시즌 활동(잔디)
}
