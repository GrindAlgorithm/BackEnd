package com.example.springboot.submission.service;

import com.example.springboot.common.tier.TierScore;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 채점 단계별 상태 갱신을 각각 독립 트랜잭션으로 커밋한다.
 * → 채점 진행 중에도 GET /submissions/{id} 폴링이 진행률/상태를 볼 수 있다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionGradingTx {

    /** 미로그인 제출 handle (SubmissionController) — 랭킹에 반영하지 않는다 */
    private static final String ANONYMOUS_HANDLE = "anonymous";

    private final SubmissionRepository submissionRepository;
    private final SeasonRankingRepository seasonRankingRepository;

    @Transactional
    public void markJudging(long submissionId) {
        submissionRepository.findById(submissionId).ifPresent(SubmissionEntity::startJudging);
    }

    @Transactional
    public void updateProgress(long submissionId, int progress, Long timeMs, Long memoryKb) {
        submissionRepository.findById(submissionId)
                .ifPresent(s -> s.updateProgress(progress, timeMs, memoryKb));
    }

    /** 오답/에러 종결 — 제출 수만 +1 */
    @Transactional
    public void finishFailed(long submissionId, SubmissionStatus status, Long timeMs, Long memoryKb) {
        submissionRepository.findById(submissionId).ifPresent(s -> {
            s.finish(status, timeMs, memoryKb);
            s.getProblem().recordSubmission(false);
        });
    }

    /** 정답 종결 — 제출 수·정답 수·맞힌 사람 수 갱신 + 첫 정답이면 시즌 랭킹 반영 */
    @Transactional
    public void finishAccepted(long submissionId, Long timeMs, Long memoryKb) {
        submissionRepository.findById(submissionId).ifPresent(s -> {
            s.finish(SubmissionStatus.ACCEPTED, timeMs, memoryKb);
            s.getProblem().recordSubmission(true);
            applyRanking(s);
        });
    }

    /**
     * 채점→랭킹 연동: 같은 문제의 첫 정답만 문제 티어 점수(TierScore)를 가산하고
     * 누적 점수로 유저 티어를 재계산(TierCut)한다. 랭킹 행이 없으면 신규 생성.
     */
    private void applyRanking(SubmissionEntity submission) {
        String handle = submission.getUserHandle();
        if (handle == null || ANONYMOUS_HANDLE.equalsIgnoreCase(handle)) {
            return;
        }

        ProblemEntity problem = submission.getProblem();
        if (problem.getSeason() == null) {
            return; // 비시즌 문제는 시즌 점수 대상이 아니다
        }

        boolean alreadySolved = submissionRepository.existsByProblem_ProblemIdAndUserHandleAndStatusAndIdNot(
                problem.getProblemId(), handle, SubmissionStatus.ACCEPTED, submission.getId());
        if (alreadySolved) {
            return; // 재정답은 점수 미가산
        }

        LocalDateTime now = LocalDateTime.now();
        SeasonRankingEntity row = seasonRankingRepository
                .findFirstBySeason_IdAndHandle(problem.getSeason().getId(), handle)
                .orElseGet(() -> seasonRankingRepository.save(
                        SeasonRankingEntity.createUnrankedEntity(problem.getSeason(), handle, now)));

        int points = TierScore.of(problem.getTierName(), problem.getTierLevel());
        row.applyAccepted(points, now);

        if (log.isInfoEnabled()) {
            log.info("applyRanking handle={} problemId={} +{}점 → score={} tier={} {}",
                    handle, problem.getProblemId(), points, row.getScore(), row.getTierName(), row.getTierLevel());
        }
    }
}
