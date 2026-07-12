package com.example.springboot.submission.service;

import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채점 단계별 상태 갱신을 각각 독립 트랜잭션으로 커밋한다.
 * → 채점 진행 중에도 GET /submissions/{id} 폴링이 진행률/상태를 볼 수 있다.
 */
@Service
@RequiredArgsConstructor
public class SubmissionGradingTx {

    private final SubmissionRepository submissionRepository;

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

    /** 정답 종결 — 제출 수·정답 수·맞힌 사람 수 갱신 */
    @Transactional
    public void finishAccepted(long submissionId, Long timeMs, Long memoryKb) {
        submissionRepository.findById(submissionId).ifPresent(s -> {
            s.finish(SubmissionStatus.ACCEPTED, timeMs, memoryKb);
            s.getProblem().recordSubmission(true);
        });
    }
}
