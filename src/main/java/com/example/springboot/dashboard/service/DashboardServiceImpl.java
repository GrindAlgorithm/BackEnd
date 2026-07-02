package com.example.springboot.dashboard.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.dashboard.dto.*;
import com.example.springboot.notice.dto.NoticeDTO;
import com.example.springboot.notice.repository.NoticeRepository;
import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_TODAY_PICKS = 3;

    // 추천 문구/유형 (추천 로직은 Deferred — MVP는 규칙 기반, 연동 문서 §2.4)
    private static final String[] PICK_REASONS = {"이어 풀기 좋은 문제", "티어업 후보", "비슷한 난이도"};
    private static final String[] PICK_REASON_TYPES = {"continue", "tier_up", "similar_level"};

    private final SeasonRepository seasonRepository;
    private final ProblemRepository problemRepository;
    private final NoticeRepository noticeRepository;

    @Override
    public DashboardDTO getDashboard() {
        LocalDate today = LocalDate.now();
        SeasonEntity currentSeason = seasonRepository
                .findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)
                .orElse(null);

        List<ProblemEntity> seasonProblems = currentSeason == null
                ? List.of()
                : problemRepository.findBySeason_IdOrderByDisplayNoAsc(currentSeason.getId());

        List<NoticeDTO> notices = noticeRepository.findAllByOrderByPublishedAtDesc().stream()
                .map(NoticeDTO::of)
                .toList();

        return new DashboardDTO(
                null,                                   // decay: 하락 공식 미확정(A1) → null
                buildTodayPicks(seasonProblems),
                buildNearbyRankingStub(),
                buildSeasonProgress(currentSeason, seasonProblems, today),
                WeeklyStatsDTO.empty(),                 // 제출 도메인 연동 전 0
                notices,
                buildSeasonActivity(currentSeason, today)
        );
    }

    /** 오늘의 추천: 현재 시즌 문제 상위 N개를 규칙 기반으로 노출 */
    private List<TodayPickDTO> buildTodayPicks(List<ProblemEntity> seasonProblems) {
        List<TodayPickDTO> picks = new ArrayList<>();
        int count = Math.min(MAX_TODAY_PICKS, seasonProblems.size());
        for (int i = 0; i < count; i++) {
            picks.add(TodayPickDTO.of(seasonProblems.get(i), PICK_REASONS[i], PICK_REASON_TYPES[i]));
        }
        return picks;
    }

    private SeasonProgressDTO buildSeasonProgress(SeasonEntity season, List<ProblemEntity> problems, LocalDate today) {
        if (season == null) {
            return null; // 진행 중인 시즌 없음
        }
        // solvedCount / nextProblemId 는 인증·제출 도메인 연동 전 기본값(0 / 첫 문제)
        String nextProblemId = problems.isEmpty() ? null : problems.get(0).getProblemId();
        return SeasonProgressDTO.of(season, today, problems.size(), 0, nextProblemId);
    }

    private ActivityCalendarDTO buildSeasonActivity(SeasonEntity season, LocalDate today) {
        LocalDate start = season == null ? today : season.getStartDate();
        return ActivityCalendarDTO.emptyFrom(start, today);
    }

    /**
     * 내 주변 순위(±2) 스텁.
     * TODO: 유저/랭킹 도메인 연동 시 실제 "내 순위 ±2"로 대체. 현재는 화면 렌더용 placeholder.
     */
    private List<NearbyRankingEntryDTO> buildNearbyRankingStub() {
        return List.of(
                new NearbyRankingEntryDTO(3, "player_up", TierRankDTO.of(TierName.GOLD, TierLevel.I), 1, false),
                new NearbyRankingEntryDTO(4, "me", TierRankDTO.of(TierName.GOLD, TierLevel.II), 2, true),
                new NearbyRankingEntryDTO(5, "player_down", TierRankDTO.of(TierName.SILVER, TierLevel.I), -1, false)
        );
    }
}
