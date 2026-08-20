package com.example.springboot.submission.service;

import com.example.springboot.language.service.LanguageService;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.submission.dto.SubmissionDTO;
import com.example.springboot.submission.dto.SubmitRequestDTO;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionGrader submissionGrader;
    private final CurrentUserProvider currentUserProvider;
    private final LanguageService languageService;

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionDTO> getSubmissions(Integer seasonId, String problemId, boolean mine) {
        // mine: 로그인 유저 handle 필터. 미로그인이면 빈 목록(연동 문서 §2.12).
        // ⚠ 최근 50건을 먼저 자른 뒤 거른다 — 내 제출이 50건 밖이면 빠질 수 있음(MVP 허용, 페이징 도입 시 개선)
        String myHandle = mine ? currentUserProvider.currentHandle() : null;
        if (mine && myHandle == null) {
            return List.of();
        }
        return findSubmissions(seasonId, problemId).stream()
                .filter(s -> myHandle == null || myHandle.equals(s.getUserHandle()))
                .map(SubmissionDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDTO getSubmission(long submissionId) {
        return submissionRepository.findById(submissionId)
                .map(SubmissionDTO::of)
                .orElse(null);
    }

    /**
     * 제출 저장을 자체 트랜잭션(repository)으로 커밋한 뒤 비동기 채점을 트리거한다.
     * (submit 을 트랜잭션으로 감싸면 커밋 전 async 가 먼저 실행돼 행을 못 찾을 수 있어 비트랜잭션)
     */
    @Override
    public Long submit(SubmitRequestDTO request, String userHandle) {
        ProblemEntity problem = problemRepository.findByProblemId(request.getProblemId()).orElse(null);
        if (problem == null) {
            return null;
        }

        // 언어 검증 + judge0 id 해석 (요건 24 — language 테이블이 단일 소유)
        int judge0LangId = languageService.resolveJudge0Id(request.getLanguage());

        int codeBytes = request.getSourceCode().getBytes(StandardCharsets.UTF_8).length;
        SubmissionEntity submission = SubmissionEntity.createSubmissionEntity(
                problem, userHandle, SubmissionStatus.QUEUED, 0, null, null,
                request.getLanguage(), codeBytes, LocalDateTime.now());
        submissionRepository.save(submission); // 자체 트랜잭션으로 즉시 커밋

        // 비동기 채점 시작 — 필요한 값만 primitive 로 넘겨 지연로딩/세션 문제 회피
        submissionGrader.grade(
                submission.getId(),
                problem.getProblemId(),
                judge0LangId,
                problem.getTimeLimitSec(),
                problem.getMemoryLimitMb() * 1024,
                request.getSourceCode());

        return submission.getId();
    }

    private List<SubmissionEntity> findSubmissions(Integer seasonId, String problemId) {
        if (seasonId != null && problemId != null) {
            return submissionRepository.findTop50ByProblem_Season_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(seasonId, problemId);
        }
        if (seasonId != null) {
            return submissionRepository.findTop50ByProblem_Season_IdOrderBySubmittedAtDesc(seasonId);
        }
        if (problemId != null) {
            return submissionRepository.findTop50ByProblem_ProblemIdOrderBySubmittedAtDesc(problemId);
        }
        return submissionRepository.findTop50ByOrderBySubmittedAtDesc();
    }
}
