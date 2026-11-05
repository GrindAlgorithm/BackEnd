package com.example.springboot.problem.service;

import com.example.springboot.problem.dto.OpenProblemDTO;
import com.example.springboot.problem.dto.ProblemBodyDTO;
import com.example.springboot.problem.dto.ProblemDetailDTO;
import com.example.springboot.problem.entity.ProblemBodyEntity;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.ProblemSampleEntity;
import com.example.springboot.problem.entity.ProblemStatus;
import com.example.springboot.problem.entity.SolveSessionEntity;
import com.example.springboot.problem.repository.ProblemBodyRepository;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.problem.repository.ProblemSampleRepository;
import com.example.springboot.problem.repository.SolveSessionRepository;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemBodyRepository problemBodyRepository;
    private final ProblemSampleRepository problemSampleRepository;
    private final SolveSessionRepository solveSessionRepository;
    private final SubmissionRepository submissionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ProblemDetailDTO getProblem(String problemId) {
        return problemRepository.findPublicByProblemId(problemId)
                .map(problem -> detailWithMyStatus(problem, currentUserProvider.currentHandle()))
                .orElse(null);
    }

    /**
     * 문제 상세 + 로그인 유저의 진행 상태. 제출 이력(submission)으로 계산한다:
     * ACCEPTED 있으면 cleared, 시도만 있으면 wip, 없으면 untried. 비로그인은 untried.
     * attemptCount=제출 수, lastTriedAt=마지막 제출 시각.
     */
    private ProblemDetailDTO detailWithMyStatus(ProblemEntity problem, String handle) {
        if (handle == null) {
            return ProblemDetailDTO.of(problem);
        }
        List<SubmissionEntity> mine = submissionRepository
                .findByProblem_ProblemIdAndUserHandle(problem.getProblemId(), handle);
        if (mine.isEmpty()) {
            return ProblemDetailDTO.of(problem);
        }
        boolean cleared = mine.stream().anyMatch(s -> s.getStatus() == SubmissionStatus.ACCEPTED);
        ProblemStatus status = cleared ? ProblemStatus.CLEARED : ProblemStatus.WIP;
        LocalDateTime lastTriedAt = mine.stream()
                .map(SubmissionEntity::getSubmittedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return ProblemDetailDTO.of(problem, status, mine.size(), lastTriedAt);
    }

    @Override
    public OpenProblemDTO openProblem(String problemId, String userHandle) {
        ProblemEntity problem = problemRepository.findPublicByProblemId(problemId).orElse(null);
        if (problem == null) {
            return null;
        }

        // 풀이 세션 발급 + 열람 시각(풀이 시작 시각) 기록 — B2
        SolveSessionEntity session = SolveSessionEntity.createSolveSessionEntity(
                UUID.randomUUID().toString(), problem, userHandle, LocalDateTime.now());
        solveSessionRepository.save(session);

        ProblemBodyEntity body = problemBodyRepository.findByProblem_ProblemId(problemId).orElse(null);
        List<ProblemSampleEntity> samples = problemSampleRepository.findByProblem_ProblemIdOrderByOrdinalAsc(problemId);

        return new OpenProblemDTO(
                session.getId(),
                session.getOpenedAt(),
                detailWithMyStatus(problem, userHandle),
                ProblemBodyDTO.of(body, samples)
        );
    }
}
