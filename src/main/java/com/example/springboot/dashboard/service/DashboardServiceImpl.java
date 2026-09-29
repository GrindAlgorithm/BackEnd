package com.example.springboot.dashboard.service;

import com.example.springboot.common.activity.ActivityStats;
import com.example.springboot.common.tier.TierScore;
import com.example.springboot.dashboard.dto.*;
import com.example.springboot.notice.dto.NoticeDTO;
import com.example.springboot.notice.repository.NoticeRepository;
import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.dto.RankingEntryDTO;
import com.example.springboot.ranking.service.RankingService;
import com.example.springboot.recommendation.repository.RecommendationRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_TODAY_PICKS = 3;
    /** 내 주변 순위 — 내 위아래 각 2명 (§2.4) */
    private static final int NEARBY_RANGE = 2;
    /** nearbyRanking.weeklyDelta 비교 기준 — 7일 전 */
    private static final int DELTA_DAYS = 7;

    private final SeasonRepository seasonRepository;
    private final ProblemRepository problemRepository;
    private final NoticeRepository noticeRepository;
    private final RecommendationRepository recommendationRepository;
    private final SubmissionRepository submissionRepository;
    private final RankingService rankingService;
    private final CurrentUserProvider currentUserProvider;

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

        // 비로그인 열람도 허용(permitAll) — 개인화 영역은 빈 값으로 내려간다
        String handle = currentUserProvider.currentHandle();
        List<SubmissionEntity> mySubmissions = handle == null
                ? List.of()
                : submissionRepository.findByUserHandleOrderBySubmittedAtDesc(handle);
        Map<ProblemEntity, LocalDateTime> firstSolvedAt = ActivityStats.firstSolvedAt(mySubmissions);

        return new DashboardDTO(
                null,                                   // decay: 하락 공식 미확정(A1) → null
                buildTodayPicks(),
                buildNearbyRanking(currentSeason, handle),
                buildSeasonProgress(currentSeason, seasonProblems, firstSolvedAt, today),
                buildWeekly(currentSeason, mySubmissions, firstSolvedAt, today),
                notices,
                buildSeasonActivity(currentSeason, firstSolvedAt, today)
        );
    }

    /** 오늘의 추천: recommendation 테이블의 추천 순위(rank_no) 순으로 상위 N개 노출. 공개 전 시즌 문제는 제외 */
    private List<TodayPickDTO> buildTodayPicks() {
        return recommendationRepository.findAllByOrderByRankNoAsc().stream()
                .filter(r -> r.getProblem().getSeason() == null || r.getProblem().getSeason().getStatus().isPublic())
                .limit(MAX_TODAY_PICKS)
                .map(r -> TodayPickDTO.of(r.getProblem(), r.getReason(), r.getReasonType()))
                .toList();
    }

    /** 클리어 수 = 내가 맞힌 시즌 문제 수, 다음 문제 = 번호순 첫 미해결 문제(전부 클리어면 null) */
    private SeasonProgressDTO buildSeasonProgress(SeasonEntity season, List<ProblemEntity> problems,
                                                  Map<ProblemEntity, LocalDateTime> firstSolvedAt, LocalDate today) {
        if (season == null) {
            return null; // 진행 중인 시즌 없음
        }
        Set<String> solvedIds = solvedProblemIds(firstSolvedAt);
        int solvedCount = (int) problems.stream().filter(p -> solvedIds.contains(p.getProblemId())).count();
        String nextProblemId = problems.stream()
                .map(ProblemEntity::getProblemId)
                .filter(id -> !solvedIds.contains(id))
                .findFirst()
                .orElse(null);
        return SeasonProgressDTO.of(season, today, problems.size(), solvedCount, nextProblemId);
    }

    /**
     * 이번주(월요일~오늘) 통계.
     * solvedCount = 이번주 처음 해결한 문제 수, scoreGained = 그중 현재 시즌 문제 점수 합,
     * accuracyRate = 이번주 제출 정답률, streakDays = 제출 기준 연속 일수(프로필과 동일 정의).
     */
    private WeeklyStatsDTO buildWeekly(SeasonEntity season, List<SubmissionEntity> submissions,
                                       Map<ProblemEntity, LocalDateTime> firstSolvedAt, LocalDate today) {
        if (submissions.isEmpty()) {
            return WeeklyStatsDTO.empty();
        }
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        int solvedCount = 0;
        int scoreGained = 0;
        for (Map.Entry<ProblemEntity, LocalDateTime> e : firstSolvedAt.entrySet()) {
            if (e.getValue().toLocalDate().isBefore(weekStart)) {
                continue;
            }
            solvedCount++;
            if (isInSeason(e.getKey(), season)) {
                scoreGained += points(e.getKey());
            }
        }

        List<SubmissionEntity> thisWeek = submissions.stream()
                .filter(s -> !s.getSubmittedAt().toLocalDate().isBefore(weekStart))
                .toList();
        long accepted = thisWeek.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();

        Set<LocalDate> activeDates = submissions.stream()
                .map(s -> s.getSubmittedAt().toLocalDate())
                .collect(Collectors.toSet());

        return new WeeklyStatsDTO(solvedCount, scoreGained,
                ActivityStats.currentStreak(activeDates, today),
                ActivityStats.percent(accepted, thisWeek.size()));
    }

    /** 이번 시즌 잔디 — count 는 그날 처음 해결한 문제 수 (프로필 잔디와 같은 의미) */
    private ActivityCalendarDTO buildSeasonActivity(SeasonEntity season, Map<ProblemEntity, LocalDateTime> firstSolvedAt,
                                                    LocalDate today) {
        LocalDate start = season == null ? today : season.getStartDate();
        Map<LocalDate, Integer> countByDate = new HashMap<>();
        for (LocalDateTime at : firstSolvedAt.values()) {
            countByDate.merge(at.toLocalDate(), 1, Integer::sum);
        }
        return ActivityCalendarDTO.of(start, today, countByDate);
    }

    /**
     * 내 주변 순위(±2) — 현재 시즌 순위표에서 내 위아래를 자른다. 미배치·비로그인이면 빈 목록.
     * weeklyDelta = 7일 전 순위 − 현재 순위(+면 상승). 순위 이력 테이블이 없으므로 7일 전 순위는
     * "현재 점수 − 최근 7일 첫 정답으로 얻은 점수"로 다시 줄 세워 추정한다.
     */
    private List<NearbyRankingEntryDTO> buildNearbyRanking(SeasonEntity season, String handle) {
        if (season == null || handle == null) {
            return List.of();
        }
        List<RankingEntryDTO> entries = rankingService.getCurrentSeasonEntries();
        int me = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (handle.equals(entries.get(i).getHandle())) {
                me = i;
                break;
            }
        }
        if (me < 0) {
            return List.of();
        }

        Map<String, Integer> pastRank = rankDaysAgo(season, entries);
        List<NearbyRankingEntryDTO> nearby = new ArrayList<>();
        for (int i = Math.max(0, me - NEARBY_RANGE); i <= Math.min(entries.size() - 1, me + NEARBY_RANGE); i++) {
            RankingEntryDTO e = entries.get(i);
            nearby.add(new NearbyRankingEntryDTO(
                    e.getRank(),
                    e.getHandle(),
                    TierRankDTO.of(e.getTierName(), e.getTierLevel()),
                    pastRank.get(e.getHandle()) - e.getRank(),
                    i == me));
        }
        return nearby;
    }

    /** 7일 전 추정 순위 (handle → rank). 동점은 현재 순서를 유지한다(안정 정렬) */
    private Map<String, Integer> rankDaysAgo(SeasonEntity season, List<RankingEntryDTO> entries) {
        LocalDateTime since = LocalDateTime.now().minusDays(DELTA_DAYS);

        // 시즌 전체 정답에서 (유저, 문제)별 최초 정답을 찾고, 그게 최근 7일이면 그 점수를 "최근 획득분"으로 본다
        Map<String, Map<ProblemEntity, LocalDateTime>> firstByUser = new HashMap<>();
        for (SubmissionEntity s : submissionRepository.findByProblem_Season_IdAndStatus(season.getId(),
                SubmissionStatus.ACCEPTED)) {
            firstByUser.computeIfAbsent(s.getUserHandle(), h -> new HashMap<>())
                    .merge(s.getProblem(), s.getSubmittedAt(), (a, b) -> a.isBefore(b) ? a : b);
        }
        Map<String, Integer> gained = new HashMap<>();
        firstByUser.forEach((h, solved) -> solved.forEach((problem, at) -> {
            if (!at.isBefore(since)) {
                gained.merge(h, points(problem), Integer::sum);
            }
        }));

        List<RankingEntryDTO> past = new ArrayList<>(entries);
        past.sort(Comparator.comparingInt((RankingEntryDTO e) -> e.getScore() - gained.getOrDefault(e.getHandle(), 0))
                .reversed());
        Map<String, Integer> rank = new HashMap<>();
        for (int i = 0; i < past.size(); i++) {
            rank.put(past.get(i).getHandle(), i + 1);
        }
        return rank;
    }

    private Set<String> solvedProblemIds(Map<ProblemEntity, LocalDateTime> firstSolvedAt) {
        return firstSolvedAt.keySet().stream().map(ProblemEntity::getProblemId).collect(Collectors.toSet());
    }

    private boolean isInSeason(ProblemEntity problem, SeasonEntity season) {
        return season != null && problem.getSeason() != null && season.getId().equals(problem.getSeason().getId());
    }

    private int points(ProblemEntity problem) {
        return TierScore.of(problem.getTierName(), problem.getTierLevel());
    }
}
