package com.example.springboot.dashboard.dto;

import com.example.springboot.notice.dto.NoticeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 홈 화면 통합 서비스 모델 — 연동 문서 §2.4. 표현 변환은 {@link DashboardResponseDTO} 에서 수행. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private DecayWarningDTO decay;
    private List<TodayPickDTO> todayPicks;
    private List<NearbyRankingEntryDTO> nearbyRanking;
    private SeasonProgressDTO season;
    private WeeklyStatsDTO weekly;
    private List<NoticeDTO> notices;
    private ActivityCalendarDTO seasonActivity;
}
