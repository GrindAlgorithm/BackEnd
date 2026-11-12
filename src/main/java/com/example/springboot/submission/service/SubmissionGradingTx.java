package com.example.springboot.submission.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.common.tier.TierScore;
import com.example.springboot.notification.entity.NotificationType;
import com.example.springboot.notification.service.NotificationService;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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

    /** 세션 검증 도입 전 미로그인 제출 handle — 랭킹에 반영하지 않는다 */
    private static final String ANONYMOUS_HANDLE = "anonymous";

    private final SubmissionRepository submissionRepository;
    private final SeasonRankingRepository seasonRankingRepository;
    private final ProblemRepository problemRepository;
    private final NotificationService notificationService;

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
            problemRepository.incrementStats(s.getProblem().getId(), 0, 0);
            notifyResult(s, status);
        });
    }

    /**
     * 정답 종결 — 제출 수·정답 수 갱신, 이 유저의 첫 정답이면 맞힌 사람 수 + 시즌 랭킹 반영.
     * <p>
     * 동시성: 같은 유저가 여러 제출을 동시에 정답 처리하면 (1) 랭킹 행 중복 생성, (2) 점수 갱신 유실,
     * (3) 같은 문제 이중 가산이 생길 수 있다. 그래서
     * <ul>
     *   <li>다른 행을 건드리기 전에 랭킹 행부터 확보·잠금한다(INSERT … ON DUPLICATE KEY UPDATE → FOR UPDATE).
     *       잠금 순서가 항상 "랭킹 행 → 문제 행"이라 교착이 생기지 않는다.</li>
     *   <li>READ_COMMITTED — 잠금을 기다린 뒤의 "이미 맞혔나" 조회가 먼저 끝난 정답을 보게 한다.
     *       (REPEATABLE READ 면 트랜잭션 첫 조회 시점 스냅샷이라 못 본다)</li>
     * </ul>
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AcceptedResult finishAccepted(long submissionId, Long timeMs, Long memoryKb) {
        SubmissionEntity submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return null;
        }
        ProblemEntity problem = submission.getProblem();
        String handle = submission.getUserHandle();

        SeasonRankingEntity ranking = isRankable(handle, problem) ? lockRankingRow(problem, handle) : null;
        // 비활동 하락 복구 대상 — 현재 시즌 랭킹 행. 문제 행보다 먼저 잠근다(잠금 순서 유지)
        SeasonRankingEntity current = currentRankingRow(problem, handle, ranking);

        boolean firstSolve = !submissionRepository.existsByProblem_ProblemIdAndUserHandleAndStatusAndIdNot(
                problem.getProblemId(), handle, SubmissionStatus.ACCEPTED, submissionId);

        submission.finish(SubmissionStatus.ACCEPTED, timeMs, memoryKb);
        problemRepository.incrementStats(problem.getId(), 1, firstSolve ? 1 : 0);
        notifyResult(submission, SubmissionStatus.ACCEPTED);

        if (ranking != null && firstSolve) {
            applyRanking(ranking, problem, handle); // 점수 기준 티어 재계산 = 하락분 복구 포함
        }
        if (current != null && !(current == ranking && firstSolve)) {
            current.restoreTier(LocalDateTime.now()); // 재풀이·비시즌 문제 정답도 활동으로 본다 (TierDecay)
        }
        return ranking == null ? null : new AcceptedResult(handle, problem.getSeason().getId());
    }

    /** 정답 반영 결과 — 랭킹에 반영된 유저·시즌 (칭호 발급 대상). 랭킹 대상이 아니면 finishAccepted 가 null 반환 */
    public record AcceptedResult(String handle, Integer seasonId) {
    }

    /** 현재 시즌 랭킹 행(잠금). 이 문제가 현재 시즌 문제면 이미 잠근 행을 그대로 쓴다. 미배치·익명이면 null */
    private SeasonRankingEntity currentRankingRow(ProblemEntity problem, String handle, SeasonRankingEntity ranking) {
        if (ranking != null && problem.getSeason().getStatus() == SeasonStatus.CURRENT) {
            return ranking;
        }
        if (handle == null || ANONYMOUS_HANDLE.equalsIgnoreCase(handle)) {
            return null;
        }
        return seasonRankingRepository.findForUpdateBySeasonStatus(SeasonStatus.CURRENT, handle).orElse(null);
    }

    /** 로그인 유저의 시즌 문제만 시즌 점수 대상 */
    private boolean isRankable(String handle, ProblemEntity problem) {
        return handle != null && !ANONYMOUS_HANDLE.equalsIgnoreCase(handle) && problem.getSeason() != null;
    }

    /**
     * 채점 종결 알림 — 제출자 본인에게 "정답/오답" 알림을 남긴다(연습문제 결과 확인).
     * 미로그인(anonymous) 제출은 알리지 않는다. actor 는 시스템 이벤트라 null.
     */
    private void notifyResult(SubmissionEntity submission, SubmissionStatus status) {
        String handle = submission.getUserHandle();
        if (handle == null || ANONYMOUS_HANDLE.equalsIgnoreCase(handle)) {
            return;
        }
        String title = submission.getProblem().getTitle();
        if (status == SubmissionStatus.ACCEPTED) {
            notificationService.notify(handle, NotificationType.PROBLEM_SOLVED, null, submission.getId(),
                    "'" + title + "' 정답입니다 🎉");
        } else {
            notificationService.notify(handle, NotificationType.PROBLEM_FAILED, null, submission.getId(),
                    "'" + title + "' " + verdictLabel(status));
        }
    }

    /** 오답/에러 상태의 한국어 라벨 */
    private String verdictLabel(SubmissionStatus status) {
        return switch (status) {
            case WRONG_ANSWER -> "틀렸습니다";
            case TIME_LIMIT -> "시간 초과입니다";
            case MEMORY_LIMIT -> "메모리 초과입니다";
            case RUNTIME_ERROR -> "런타임 에러입니다";
            case COMPILE_ERROR -> "컴파일 에러입니다";
            default -> "채점에 실패했습니다";
        };
    }

    /** 랭킹 행이 없으면 미배치 행으로 만들고(있으면 그대로), 쓰기 잠금을 잡아 반환한다 */
    private SeasonRankingEntity lockRankingRow(ProblemEntity problem, String handle) {
        Integer seasonId = problem.getSeason().getId();
        seasonRankingRepository.insertIfAbsent(seasonId, handle,
                TierName.BRONZE.name(), TierLevel.V.name(), LocalDateTime.now());
        return seasonRankingRepository.findForUpdate(seasonId, handle)
                .orElseThrow(() -> new IllegalStateException(
                        "랭킹 행 확보 실패 seasonId=" + seasonId + " handle=" + handle));
    }

    /** 첫 정답 — 문제 티어 점수(TierScore)를 가산하고 누적 점수로 유저 티어를 재계산(TierCut)한다 */
    private void applyRanking(SeasonRankingEntity row, ProblemEntity problem, String handle) {
        int points = TierScore.of(problem.getTierName(), problem.getTierLevel());
        row.applyAccepted(points, LocalDateTime.now());

        if (log.isInfoEnabled()) {
            log.info("applyRanking handle={} problemId={} +{}점 → score={} tier={} {}",
                    handle, problem.getProblemId(), points, row.getScore(), row.getTierName(), row.getTierLevel());
        }
    }
}
