package com.example.springboot.submission.service;

import com.example.springboot.judge0.Judge0Client;
import com.example.springboot.judge0.Judge0ExecRequest;
import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.language.service.LanguageService;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.problem.service.SolveSessionGuard;
import com.example.springboot.submission.dto.RunRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RunServiceImpl implements RunService {

    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;
    private final LanguageService languageService;
    private final SolveSessionGuard solveSessionGuard;

    @Override
    public Judge0Execution run(RunRequestDTO request, String userHandle) {
        ProblemEntity problem = problemRepository.findPublicByProblemId(request.getProblemId()).orElse(null);
        if (problem == null) {
            return null;
        }
        // 본문을 연 세션에서만 실행 허용 — judge0 를 범용 실행기로 쓰는 남용도 함께 막는다
        solveSessionGuard.verify(request.getSolveSessionId(), problem.getProblemId(), userHandle);
        // 실행은 채점이 아니므로 expectedOutput 없이 stdin 만으로 실행 (연동 문서 §2.9)
        Judge0ExecRequest exec = new Judge0ExecRequest(
                languageService.resolveJudge0Id(request.getLanguage()),
                request.getSourceCode(),
                request.getStdin(),
                null,
                problem.getTimeLimitSec(),
                problem.getMemoryLimitMb() * 1024
        );
        return judge0Client.execute(exec);
    }
}
