package com.example.springboot.title.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.RewardConditionType;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonRewardEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.season.repository.SeasonRewardRepository;
import com.example.springboot.submission.entity.LanguageCode;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.title.entity.UserTitleEntity;
import com.example.springboot.title.repository.UserTitleRepository;
import com.example.springboot.user.dto.TitleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** 칭호 발급 (A4) — 리워드 달성 + 골드 이상일 때만, 챔피언은 시즌 종료 후에만 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TitleServiceTest {

    @Mock private SeasonRepository seasonRepository;
    @Mock private SeasonRewardRepository seasonRewardRepository;
    @Mock private SeasonRankingRepository rankingRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private UserTitleRepository userTitleRepository;

    @InjectMocks private TitleService service;

    private SeasonEntity season;
    private ProblemEntity p1;
    private ProblemEntity p2;
    private SeasonRewardEntity first;
    private SeasonRewardEntity clearAll;
    private SeasonRewardEntity champion;

    @BeforeEach
    void setUp() {
        season = SeasonEntity.createSeasonEntity("Season 3", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31),
                SeasonStatus.CURRENT);
        ReflectionTestUtils.setField(season, "id", 3);
        p1 = problem("a", "A");
        p2 = problem("b", "B");
        first = SeasonRewardEntity.createSeasonRewardEntity("s3_first", season, "첫 발걸음", "silver", "1개 클리어",
                RewardConditionType.CLEAR_COUNT, 1, 1);
        clearAll = SeasonRewardEntity.createSeasonRewardEntity("s3_clear", season, "클리어", "platinum", "전부 클리어",
                RewardConditionType.CLEAR_ALL, null, 2);
        champion = SeasonRewardEntity.createSeasonRewardEntity("s3_champion", season, "챔피언", "gold", "1위",
                RewardConditionType.CHAMPION, null, 3);
        when(seasonRepository.findById(3)).thenReturn(Optional.of(season));
        when(seasonRewardRepository.findBySeason_IdOrderBySortOrderAsc(3)).thenReturn(List.of(first, clearAll, champion));
        when(problemRepository.findBySeason_IdOrderByDisplayNoAsc(3)).thenReturn(List.of(p1, p2));
        when(userTitleRepository.insertIfAbsent(anyString(), anyString(), any())).thenReturn(1);
    }

    private ProblemEntity problem(String id, String no) {
        return ProblemEntity.createProblemEntity(id, no, id, TierName.GOLD, TierLevel.V, season, List.of(),
                1, 256, null, 0, 0, 0, 0);
    }

    private SubmissionEntity accepted(String handle, ProblemEntity p) {
        return SubmissionEntity.createSubmissionEntity(p, handle, null, SubmissionStatus.ACCEPTED, null, null, null,
                LanguageCode.PYTHON3, 10, LocalDateTime.now());
    }

    private SeasonRankingEntity row(String handle, int score, int solved) {
        return SeasonRankingEntity.createSeasonRankingEntity(season, handle, TierName.BRONZE, TierLevel.V,
                score, solved, LocalDateTime.now());
    }

    @Test
    void belowGoldGetsNothingEvenIfConditionsMet() {
        when(rankingRepository.findBySeason_IdOrderByScoreDesc(3)).thenReturn(List.of(row("silver", 1299, 2)));
        when(submissionRepository.findByProblem_Season_IdAndStatus(3, SubmissionStatus.ACCEPTED))
                .thenReturn(List.of(accepted("silver", p1), accepted("silver", p2)));

        assertEquals(0, service.awardSeason(3));
        verify(userTitleRepository, never()).insertIfAbsent(anyString(), anyString(), any());
    }

    @Test
    void goldUserGetsAchievedRewardsButNotChampionWhileSeasonRuns() {
        when(rankingRepository.findBySeason_IdOrderByScoreDesc(3)).thenReturn(List.of(row("gold", 1300, 1)));
        when(submissionRepository.findByProblem_Season_IdAndStatus(3, SubmissionStatus.ACCEPTED))
                .thenReturn(List.of(accepted("gold", p1), accepted("gold", p1)));

        assertEquals(1, service.awardSeason(3));
        verify(userTitleRepository).insertIfAbsent(eq("gold"), eq("s3_first"), any());
        verify(userTitleRepository, never()).insertIfAbsent(eq("gold"), eq("s3_clear"), any());
        verify(userTitleRepository, never()).insertIfAbsent(eq("gold"), eq("s3_champion"), any());
    }

    @Test
    void championIssuedOnlyAfterSeasonEnds() {
        season.changeStatus(SeasonStatus.PAST);
        when(rankingRepository.findBySeason_IdOrderByScoreDesc(3))
                .thenReturn(List.of(row("top", 2000, 2), row("second", 1900, 2)));
        when(submissionRepository.findByProblem_Season_IdAndStatus(3, SubmissionStatus.ACCEPTED))
                .thenReturn(List.of(accepted("top", p1), accepted("top", p2), accepted("second", p1), accepted("second", p2)));

        service.awardSeason(3);

        verify(userTitleRepository).insertIfAbsent(eq("top"), eq("s3_champion"), any());
        verify(userTitleRepository).insertIfAbsent(eq("top"), eq("s3_clear"), any());
        verify(userTitleRepository, never()).insertIfAbsent(eq("second"), eq("s3_champion"), any());
        verify(userTitleRepository).insertIfAbsent(eq("second"), eq("s3_clear"), any());
    }

    @Test
    void awardUserCountsOnlyThatUsersDistinctClears() {
        when(rankingRepository.findBySeason_IdOrderByScoreDesc(3)).thenReturn(List.of(row("other", 3000, 2), row("me", 1400, 2)));
        when(submissionRepository.findByProblem_Season_IdAndUserHandle(3, "me"))
                .thenReturn(List.of(accepted("me", p1), accepted("me", p2)));

        assertEquals(2, service.awardUser("me", 3));
        verify(userTitleRepository).insertIfAbsent(eq("me"), eq("s3_first"), any());
        verify(userTitleRepository).insertIfAbsent(eq("me"), eq("s3_clear"), any());
    }

    @Test
    void titlesForOthersShowOnlyOwnedAndMarkExpired() {
        season.changeStatus(SeasonStatus.PAST);
        when(seasonRepository.findAllByOrderByIdDesc()).thenReturn(List.of(season));
        when(userTitleRepository.findByHandle("me"))
                .thenReturn(List.of(new UserTitleEntity(1L, "me", "s3_first", LocalDateTime.now())));

        List<TitleResponseDTO> mine = service.titlesFor("me", true);
        assertEquals(3, mine.size());
        assertTrue(mine.get(0).isOwned());
        assertFalse(mine.get(0).isExpired());
        assertTrue(mine.get(1).isExpired()); // 끝난 시즌의 미보유 칭호
        assertEquals(3, mine.get(0).getFromSeason());

        List<TitleResponseDTO> theirs = service.titlesFor("me", false);
        assertEquals(1, theirs.size());
        assertEquals("s3_first", theirs.get(0).getId());
    }
}
