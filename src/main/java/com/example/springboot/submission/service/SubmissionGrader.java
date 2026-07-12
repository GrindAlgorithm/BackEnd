package com.example.springboot.submission.service;

import com.example.springboot.judge0.Judge0Client;
import com.example.springboot.judge0.Judge0ExecRequest;
import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.judge0.Judge0Verdict;
import com.example.springboot.problem.entity.ProblemSampleEntity;
import com.example.springboot.problem.repository.ProblemSampleRepository;
import com.example.springboot.submission.entity.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 비동기 채점 오케스트레이터 (연동 문서 §2.11 폴링 대응).
 * 테스트케이스(현재는 예제=problem_sample)를 순회하며 Judge0로 채점하고,
 * 케이스마다 진행률을 독립 트랜잭션으로 커밋한다. 첫 실패 케이스에서 종결.
 * ⚠ MVP: 히든 테스트케이스가 아직 없어 예제로 채점한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionGrader {

    private final Judge0Client judge0Client;
    private final ProblemSampleRepository problemSampleRepository;
    private final SubmissionGradingTx tx;

    @Async
    public void grade(long submissionId, String problemId, int judge0LangId,
                      int cpuTimeLimitSec, int memoryLimitKb, String sourceCode) {
        tx.markJudging(submissionId);

        List<ProblemSampleEntity> tests = problemSampleRepository.findByProblem_ProblemIdOrderByOrdinalAsc(problemId);
        if (tests.isEmpty()) {
            tx.finishAccepted(submissionId, 0L, 0L);
            return;
        }

        long maxTimeMs = 0;
        long maxMemoryKb = 0;
        int total = tests.size();
        int done = 0;

        for (ProblemSampleEntity test : tests) {
            Judge0Execution exec = judge0Client.execute(new Judge0ExecRequest(
                    judge0LangId, sourceCode, test.getInput(), test.getOutput(),
                    cpuTimeLimitSec, memoryLimitKb));
            done++;
            if (exec.timeMs() != null) {
                maxTimeMs = Math.max(maxTimeMs, exec.timeMs());
            }
            if (exec.memoryKb() != null) {
                maxMemoryKb = Math.max(maxMemoryKb, exec.memoryKb());
            }

            if (exec.verdict() != Judge0Verdict.ACCEPTED) {
                tx.finishFailed(submissionId, toStatus(exec.verdict()), maxTimeMs, maxMemoryKb);
                if (log.isInfoEnabled()) {
                    log.info("grade done submissionId={} verdict={} at case {}/{}",
                            submissionId, exec.verdict(), done, total);
                }
                return;
            }
            tx.updateProgress(submissionId, (int) (done * 100L / total), maxTimeMs, maxMemoryKb);
        }

        tx.finishAccepted(submissionId, maxTimeMs, maxMemoryKb);
        if (log.isInfoEnabled()) {
            log.info("grade done submissionId={} ACCEPTED ({} cases)", submissionId, total);
        }
    }

    private SubmissionStatus toStatus(Judge0Verdict verdict) {
        return switch (verdict) {
            case ACCEPTED -> SubmissionStatus.ACCEPTED;
            case WRONG_ANSWER -> SubmissionStatus.WRONG_ANSWER;
            case TIME_LIMIT -> SubmissionStatus.TIME_LIMIT;
            case MEMORY_LIMIT -> SubmissionStatus.MEMORY_LIMIT;
            case COMPILE_ERROR -> SubmissionStatus.COMPILE_ERROR;
            default -> SubmissionStatus.RUNTIME_ERROR; // RUNTIME_ERROR, INTERNAL_ERROR
        };
    }
}
