package com.example.springboot.user.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.submission.entity.LanguageCode;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.dto.UserProfileDTO;
import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** 프로필 통계/잔디/최근 목록 계산 검증 (연동 문서 §2.15) — Spring/DB 불필요 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private SeasonRankingRepository rankingRepository;
    @Mock private SeasonRepository seasonRepository;

    @InjectMocks private UserProfileServiceImpl service;

    private static final LocalDateTime JOINED = LocalDateTime.of(2026, 1, 1, 9, 0);

    private ProblemEntity problem(String problemId, String displayNo) {
        return ProblemEntity.createProblemEntity(problemId, displayNo, "제목-" + problemId,
                TierName.GOLD, TierLevel.IV, null, List.of(), 2, 256, null, 0, 0, 0, 0);
    }

    private SubmissionEntity submission(ProblemEntity p, SubmissionStatus status, LocalDateTime at) {
        return SubmissionEntity.createSubmissionEntity(p, "park", status, null, null, null,
                LanguageCode.JAVA11, 100, at);
    }

    @Test
    void unknownHandleReturnsNull() {
        when(userRepository.findByHandle("nobody")).thenReturn(Optional.empty());

        assertNull(service.getProfile("nobody", null));
    }

    @Test
    void statsCountDistinctSolvedProblemsAndAccuracy() {
        ProblemEntity a = problem("conquest", "C");
        ProblemEntity b = problem("distance", "D");
        LocalDateTime now = LocalDateTime.now();
        // a: 오답 1 + 정답 2(재제출) / b: 오답 1 → solved 1, 제출 4, 정답 2건 → 정답률 50.0
        List<SubmissionEntity> subs = List.of(
                submission(a, SubmissionStatus.ACCEPTED, now.minusHours(1)),
                submission(a, SubmissionStatus.ACCEPTED, now.minusHours(2)),
                submission(a, SubmissionStatus.WRONG_ANSWER, now.minusHours(3)),
                submission(b, SubmissionStatus.WRONG_ANSWER, now.minusHours(4)));

        when(userRepository.findByHandle("park"))
                .thenReturn(Optional.of(UserEntity.createUserEntity("park", "p@t.dev", "x", JOINED)));
        when(submissionRepository.findByUserHandleOrderBySubmittedAtDesc("park")).thenReturn(subs);
        when(seasonRepository.findFirstByStatusOrderByIdDesc(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        UserProfileDTO profile = service.getProfile("park", "park");

        assertTrue(profile.isMe());
        assertEquals(1, profile.getStats().getSolvedCount());
        assertEquals(4, profile.getStats().getSubmissionCount());
        assertEquals(50.0, profile.getStats().getAccuracyRate());
        assertEquals(4.0, profile.getStats().getAvgAttempts());
        // 오늘 활동 → 스트릭 1일
        assertEquals(1, profile.getStats().getStreakDays());
        assertEquals(1, profile.getStats().getLongestStreakDays());
        // 최근 해결: 최초 정답 시각 기준 1건
        assertEquals(1, profile.getRecentSolved().size());
        assertEquals("conquest", profile.getRecentSolved().get(0).getProblemId());
        assertEquals(now.minusHours(2), profile.getRecentSolved().get(0).getSolvedAt());
        // 랭킹 미배치 → 시즌 필드 비어 있음
        assertNull(profile.getSeasonTierName());
        assertNull(profile.getSeasonRank());
        assertEquals(0, profile.getSeasonScore());
    }

    @Test
    void activityCountsFirstSolvesPerDayWithin52Weeks() {
        ProblemEntity a = problem("conquest", "C");
        ProblemEntity b = problem("distance", "D");
        LocalDateTime today = LocalDate.now().atTime(10, 0);
        List<SubmissionEntity> subs = List.of(
                submission(a, SubmissionStatus.ACCEPTED, today),
                submission(b, SubmissionStatus.ACCEPTED, today.minusHours(1)),
                // 52주(364일) 창 밖 — 잔디에서 제외
                submission(problem("stopwatch", "A"), SubmissionStatus.ACCEPTED, today.minusDays(400)));

        when(userRepository.findByHandle(anyString()))
                .thenReturn(Optional.of(UserEntity.createUserEntity("park", "p@t.dev", "x", JOINED)));
        when(submissionRepository.findByUserHandleOrderBySubmittedAtDesc(anyString())).thenReturn(subs);
        when(seasonRepository.findFirstByStatusOrderByIdDesc(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        UserProfileDTO profile = service.getProfile("park", null);

        assertFalse(profile.isMe());
        assertEquals(364, profile.getActivity().getDays().size());
        assertEquals(1, profile.getActivity().getActiveDays());
        assertEquals(2.0, profile.getActivity().getAvgPerDay());
        var lastDay = profile.getActivity().getDays().get(363);
        assertEquals(LocalDate.now().toString(), lastDay.getDate());
        assertEquals(2, lastDay.getCount());
        assertEquals(2, lastDay.getLevel());
    }
}
