package com.example.springboot.problem.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.dto.ProblemDetailDTO;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.ProblemStatus;
import com.example.springboot.problem.repository.ProblemBodyRepository;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.problem.repository.ProblemSampleRepository;
import com.example.springboot.problem.repository.SolveSessionRepository;
import com.example.springboot.submission.entity.LanguageCode;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** GET /problems/{id} 의 myStatus — 제출 이력이 상세에 반영돼야 한다(재진입 시 초기화되지 않게) */
@ExtendWith(MockitoExtension.class)
class ProblemServiceImplTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ProblemBodyRepository problemBodyRepository;
    @Mock private ProblemSampleRepository problemSampleRepository;
    @Mock private SolveSessionRepository solveSessionRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks private ProblemServiceImpl service;

    private final ProblemEntity problem = ProblemEntity.createProblemEntity(
            "p1", "A", "문제", TierName.SILVER, TierLevel.III, null, List.of(), 2, 256, null, 0, 0, 0, 0);

    @Test
    void accepted_history_makesClearedWithAttemptsAndLastTriedAt() {
        when(problemRepository.findPublicByProblemId("p1")).thenReturn(Optional.of(problem));
        when(currentUserProvider.currentHandle()).thenReturn("me");
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 11, 2, 10, 0);
        when(submissionRepository.findByProblem_ProblemIdAndUserHandle("p1", "me")).thenReturn(List.of(
                sub(SubmissionStatus.WRONG_ANSWER, t1),
                sub(SubmissionStatus.ACCEPTED, t2)));

        ProblemDetailDTO dto = service.getProblem("p1");

        assertEquals(ProblemStatus.CLEARED, dto.getMyStatus());
        assertEquals(2, dto.getAttemptCount());
        assertEquals(t2, dto.getLastTriedAt());
    }

    @Test
    void onlyFailed_history_makesWip() {
        when(problemRepository.findPublicByProblemId("p1")).thenReturn(Optional.of(problem));
        when(currentUserProvider.currentHandle()).thenReturn("me");
        when(submissionRepository.findByProblem_ProblemIdAndUserHandle("p1", "me")).thenReturn(List.of(
                sub(SubmissionStatus.WRONG_ANSWER, LocalDateTime.of(2026, 11, 1, 10, 0))));

        ProblemDetailDTO dto = service.getProblem("p1");

        assertEquals(ProblemStatus.WIP, dto.getMyStatus());
        assertEquals(1, dto.getAttemptCount());
    }

    @Test
    void anonymous_isUntried_andDoesNotQuerySubmissions() {
        when(problemRepository.findPublicByProblemId("p1")).thenReturn(Optional.of(problem));
        when(currentUserProvider.currentHandle()).thenReturn(null);

        ProblemDetailDTO dto = service.getProblem("p1");

        assertEquals(ProblemStatus.UNTRIED, dto.getMyStatus());
        assertEquals(0, dto.getAttemptCount());
        assertNull(dto.getLastTriedAt());
        verify(submissionRepository, never()).findByProblem_ProblemIdAndUserHandle(any(), any());
    }

    private SubmissionEntity sub(SubmissionStatus status, LocalDateTime at) {
        return SubmissionEntity.createSubmissionEntity(problem, "me", null, status, null, null, null,
                LanguageCode.JAVA11, 100, at);
    }
}
