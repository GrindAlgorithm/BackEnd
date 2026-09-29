package com.example.springboot.submission.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.common.tier.TierScore;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.submission.entity.LanguageCode;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 정답 종결 로직 — 랭킹 행 확보·잠금이 다른 갱신보다 먼저 오는지, 첫 정답만 가산되는지.
 * (실제 동시성 직렬화는 DB 잠금이 담당하므로 여기서는 호출 순서와 분기를 검증한다)
 */
@ExtendWith(MockitoExtension.class)
class SubmissionGradingTxTest {

    @Mock private SubmissionRepository submissionRepository;
    @Mock private SeasonRankingRepository seasonRankingRepository;
    @Mock private ProblemRepository problemRepository;

    @InjectMocks private SubmissionGradingTx tx;

    private SeasonEntity season;
    private ProblemEntity problem;
    private SeasonRankingEntity ranking;

    @BeforeEach
    void setUp() {
        season = SeasonEntity.createSeasonEntity("Season 2", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30),
                SeasonStatus.CURRENT);
        ReflectionTestUtils.setField(season, "id", 2);
        problem = ProblemEntity.createProblemEntity("conquest", "A", "정복", TierName.GOLD, TierLevel.IV, season,
                List.of(), 2, 256, null, 0, 0, 0, 0);
        ReflectionTestUtils.setField(problem, "id", 10L);
        ranking = SeasonRankingEntity.createSeasonRankingEntity(season, "park", TierName.BRONZE, TierLevel.V,
                0, 0, LocalDateTime.now());
    }

    private SubmissionEntity givenSubmission(long id, String handle, ProblemEntity p) {
        SubmissionEntity s = SubmissionEntity.createSubmissionEntity(p, handle, "sess", SubmissionStatus.JUDGING,
                0, null, null, LanguageCode.JAVA11, 100, LocalDateTime.now());
        ReflectionTestUtils.setField(s, "id", id);
        when(submissionRepository.findById(id)).thenReturn(Optional.of(s));
        return s;
    }

    private void givenAlreadySolved(boolean solved) {
        when(submissionRepository.existsByProblem_ProblemIdAndUserHandleAndStatusAndIdNot(
                eq("conquest"), eq("park"), eq(SubmissionStatus.ACCEPTED), anyLong())).thenReturn(solved);
    }

    @Test
    void firstSolveLocksRankingRowBeforeAnyOtherWriteAndAddsPoints() {
        SubmissionEntity s = givenSubmission(1L, "park", problem);
        when(seasonRankingRepository.findForUpdate(2, "park")).thenReturn(Optional.of(ranking));
        givenAlreadySolved(false);

        tx.finishAccepted(1L, 90L, 1000L);

        InOrder order = inOrder(seasonRankingRepository, submissionRepository, problemRepository);
        order.verify(seasonRankingRepository).insertIfAbsent(eq(2), eq("park"), eq("BRONZE"), eq("V"), any());
        order.verify(seasonRankingRepository).findForUpdate(2, "park");
        order.verify(submissionRepository).existsByProblem_ProblemIdAndUserHandleAndStatusAndIdNot(
                "conquest", "park", SubmissionStatus.ACCEPTED, 1L);
        order.verify(problemRepository).incrementStats(10L, 1, 1);

        assertEquals(SubmissionStatus.ACCEPTED, s.getStatus());
        assertEquals(TierScore.of(TierName.GOLD, TierLevel.IV), ranking.getScore());
        assertEquals(1, ranking.getSolvedCount());
    }

    @Test
    void repeatSolveCountsSubmissionButNotSolverOrPoints() {
        givenSubmission(2L, "park", problem);
        when(seasonRankingRepository.findForUpdate(2, "park")).thenReturn(Optional.of(ranking));
        givenAlreadySolved(true);

        tx.finishAccepted(2L, 90L, 1000L);

        verify(problemRepository).incrementStats(10L, 1, 0);
        assertEquals(0, ranking.getScore());
        assertEquals(0, ranking.getSolvedCount());
    }

    @Test
    void nonSeasonProblemSkipsRanking() {
        ProblemEntity practice = ProblemEntity.createProblemEntity("conquest", "1001", "연습", TierName.GOLD,
                TierLevel.IV, null, List.of(), 2, 256, null, 0, 0, 0, 0);
        ReflectionTestUtils.setField(practice, "id", 11L);
        givenSubmission(3L, "park", practice);
        givenAlreadySolved(false);

        tx.finishAccepted(3L, 90L, 1000L);

        verifyNoInteractions(seasonRankingRepository);
        verify(problemRepository).incrementStats(11L, 1, 1);
    }

    @Test
    void failedSubmissionOnlyCountsSubmission() {
        SubmissionEntity s = givenSubmission(4L, "park", problem);

        tx.finishFailed(4L, SubmissionStatus.WRONG_ANSWER, 90L, 1000L);

        assertEquals(SubmissionStatus.WRONG_ANSWER, s.getStatus());
        verify(problemRepository).incrementStats(10L, 0, 0);
        verifyNoInteractions(seasonRankingRepository);
    }
}
