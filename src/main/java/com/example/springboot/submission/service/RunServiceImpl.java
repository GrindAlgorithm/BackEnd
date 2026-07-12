package com.example.springboot.submission.service;

import com.example.springboot.judge0.Judge0Client;
import com.example.springboot.judge0.Judge0ExecRequest;
import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.repository.ProblemRepository;
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

    @Override
    public Judge0Execution run(RunRequestDTO request) {
        ProblemEntity problem = problemRepository.findByProblemId(request.getProblemId()).orElse(null);
        if (problem == null) {
            return null;
        }
        // 실행은 채점이 아니므로 expectedOutput 없이 stdin 만으로 실행 (연동 문서 §2.9)
        Judge0ExecRequest exec = new Judge0ExecRequest(
                request.getLanguage().getJudge0Id(),
                request.getSourceCode(),
                request.getStdin(),
                null,
                problem.getTimeLimitSec(),
                problem.getMemoryLimitMb() * 1024
        );
        return judge0Client.execute(exec);
    }
}
