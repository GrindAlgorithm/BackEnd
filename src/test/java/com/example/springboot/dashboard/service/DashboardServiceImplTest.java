package com.example.springboot.dashboard.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.common.tier.TierScore;
import com.example.springboot.dashboard.dto.DashboardDTO;
import com.example.springboot.dashboard.dto.NearbyRankingEntryDTO;
import com.example.springboot.notice.repository.NoticeRepository;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.dto.RankingEntryDTO;
import com.example.springboot.ranking.service.RankingService;
import com.example.springboot.recommendation.repository.RecommendationRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.submission.entity.LanguageCode;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 홈 대시보드 개인화 (§2.4) — 내 주변 순위·7일 변동, 시즌 진행, 이번주 통계, 잔디 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private SeasonRepository seasonRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private RankingService rankingService;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks private DashboardServiceImpl service;

    /** "오늘" 정오 — 실제 현재 시각을 쓰면 자정 직후 실행 시 몇 분 전이 어제가 돼 스트릭·이번주 집계가 틀어진다 */
    private final LocalDateTime now = LocalDate.now().atTime(12, 0);
    private ProblemEntity conquest;
    private ProblemEntity distance;
    private ProblemEntity forced;
    private ProblemEntity hard;

    @BeforeEach
    void setUp() {
        // 시즌 시작일은 잔디 창(최대 84일)보다 넉넉히 과거로 — 실행 날짜와 무관하게 같은 결과
        SeasonEntity season = SeasonEntity.createSeasonEntity("Season 2", LocalDate.now().minusDays(60),
                LocalDate.now().plusDays(30), SeasonStatus.CURRENT);
        ReflectionTestUtils.setField(season, "id", 2);
        conquest = problem("conquest", "A", TierName.GOLD, TierLevel.IV, season);
        distance = problem("distance", "B", TierName.SILVER, TierLevel.II, season);
        forced = problem("forced", "C", TierName.GOLD, TierLevel.I, season);
        hard = problem("hard", "D", TierName.DIAMOND, TierLevel.I, season);

        when(seasonRepository.findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)).thenReturn(Optional.of(season));
        when(problemRepository.findBySeason_IdOrderByDisplayNoAsc(2))
                .thenReturn(List.of(conquest, distance, forced, hard));
        when(noticeRepository.findAllByOrderByPublishedAtDesc()).thenReturn(List.of());
        when(recommendationRepository.findAllByOrderByRankNoAsc()).thenReturn(List.of());
    }

    private ProblemEntity problem(String id, String no, TierName name, TierLevel level, SeasonEntity season) {
        return ProblemEntity.createProblemEntity(id, no, id, name, level, season, List.of(), 2, 256, null, 0, 0, 0, 0);
    }

    private SubmissionEntity sub(ProblemEntity p, String handle, SubmissionStatus status, LocalDateTime at) {
        return SubmissionEntity.createSubmissionEntity(p, handle, null, status, null, null, null,
                LanguageCode.JAVA11, 100, at);
    }

    private RankingEntryDTO entry(int rank, String handle, int score) {
        return new RankingEntryDTO(rank, handle, TierName.GOLD, TierLevel.V, score, 1, now);
    }

    @Test
    void loggedInUserGetsPersonalizedDashboard() {
        when(currentUserProvider.currentHandle()).thenReturn("park");
        when(submissionRepository.findByUserHandleOrderBySubmittedAtDesc("park")).thenReturn(List.of(
                sub(distance, "park", SubmissionStatus.ACCEPTED, now),
                sub(distance, "park", SubmissionStatus.WRONG_ANSWER, now.minusMinutes(5)),
                sub(conquest, "park", SubmissionStatus.ACCEPTED, now.minusDays(10))));
        when(rankingService.getCurrentSeasonEntries()).thenReturn(new ArrayList<>(List.of(
                entry(1, "god", 500), entry(2, "lee", 400), entry(3, "kim", 300),
                entry(4, "park", 200), entry(5, "choi", 150), entry(6, "han", 100))));
        // kim 은 최근 7일에 330점짜리를 처음 풀어 3위로 올라왔다 → 7일 전엔 꼴찌(6위)
        when(submissionRepository.findByProblem_Season_IdAndStatus(2, SubmissionStatus.ACCEPTED)).thenReturn(List.of(
                sub(hard, "kim", SubmissionStatus.ACCEPTED, now.minusDays(2)),
                sub(conquest, "park", SubmissionStatus.ACCEPTED, now.minusDays(10))));

        DashboardDTO d = service.getDashboard();

        // 내 주변 순위: 내 위아래 2명씩, 7일 전 순위 − 현재 순위
        List<NearbyRankingEntryDTO> nearby = d.getNearbyRanking();
        assertEquals(List.of("lee", "kim", "park", "choi", "han"),
                nearby.stream().map(NearbyRankingEntryDTO::getHandle).toList());
        assertEquals(List.of(0, 3, -1, -1, -1),
                nearby.stream().map(NearbyRankingEntryDTO::getWeeklyDelta).toList());
        assertEquals(List.of(false, false, true, false, false),
                nearby.stream().map(NearbyRankingEntryDTO::isMe).toList());

        // 시즌 진행: 4문제 중 2개 클리어, 다음 문제는 번호순 첫 미해결
        assertEquals(2, d.getSeason().getSolvedCount());
        assertEquals(4, d.getSeason().getTotalCount());
        assertEquals("forced", d.getSeason().getNextProblemId());
        assertEquals(0.5, d.getSeason().getProgressRatio());

        // 이번주: distance 첫 정답만(conquest 는 10일 전), 제출 2건 중 1건 정답
        assertEquals(1, d.getWeekly().getSolvedCount());
        assertEquals(TierScore.of(TierName.SILVER, TierLevel.II), d.getWeekly().getScoreGained());
        assertEquals(50.0, d.getWeekly().getAccuracyRate());
        assertEquals(1, d.getWeekly().getStreakDays());

        // 잔디: 오늘 1문제 + 10일 전 1문제
        assertEquals(2, d.getSeasonActivity().getActiveDays());
        assertEquals(1.0, d.getSeasonActivity().getAvgPerDay());
        var days = d.getSeasonActivity().getDays();
        assertEquals(LocalDate.now().toString(), days.get(days.size() - 1).getDate());
        assertEquals(1, days.get(days.size() - 1).getCount());
    }

    @Test
    void topRankedUserSeesOnlyRowsBelow() {
        when(currentUserProvider.currentHandle()).thenReturn("god");
        when(submissionRepository.findByUserHandleOrderBySubmittedAtDesc("god")).thenReturn(List.of());
        when(rankingService.getCurrentSeasonEntries()).thenReturn(new ArrayList<>(List.of(
                entry(1, "god", 500), entry(2, "lee", 400), entry(3, "kim", 300), entry(4, "park", 200))));
        when(submissionRepository.findByProblem_Season_IdAndStatus(2, SubmissionStatus.ACCEPTED)).thenReturn(List.of());

        List<NearbyRankingEntryDTO> nearby = service.getDashboard().getNearbyRanking();

        assertEquals(List.of("god", "lee", "kim"), nearby.stream().map(NearbyRankingEntryDTO::getHandle).toList());
        assertTrue(nearby.get(0).isMe());
    }

    @Test
    void unrankedUserGetsEmptyNearbyRanking() {
        when(currentUserProvider.currentHandle()).thenReturn("newbie");
        when(submissionRepository.findByUserHandleOrderBySubmittedAtDesc("newbie")).thenReturn(List.of());
        when(rankingService.getCurrentSeasonEntries()).thenReturn(List.of(entry(1, "god", 500)));

        assertTrue(service.getDashboard().getNearbyRanking().isEmpty());
    }

    @Test
    void anonymousViewerGetsEmptyPersonalSections() {
        when(currentUserProvider.currentHandle()).thenReturn(null);

        DashboardDTO d = service.getDashboard();

        assertTrue(d.getNearbyRanking().isEmpty());
        assertEquals(0, d.getWeekly().getSolvedCount());
        assertEquals(0, d.getSeason().getSolvedCount());
        assertEquals("conquest", d.getSeason().getNextProblemId());
        assertEquals(0, d.getSeasonActivity().getActiveDays());
        verifyNoInteractions(submissionRepository, rankingService);
    }
}
