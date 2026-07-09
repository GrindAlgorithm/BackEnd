package com.example.springboot.problem.service;

import com.example.springboot.problem.dto.OpenProblemDTO;
import com.example.springboot.problem.dto.ProblemBodyDTO;
import com.example.springboot.problem.dto.ProblemDetailDTO;
import com.example.springboot.problem.entity.ProblemBodyEntity;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.problem.entity.ProblemSampleEntity;
import com.example.springboot.problem.entity.SolveSessionEntity;
import com.example.springboot.problem.repository.ProblemBodyRepository;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.problem.repository.ProblemSampleRepository;
import com.example.springboot.problem.repository.SolveSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Override
    public ProblemDetailDTO getProblem(String problemId) {
        return problemRepository.findByProblemId(problemId)
                .map(ProblemDetailDTO::of)
                .orElse(null);
    }

    @Override
    public OpenProblemDTO openProblem(String problemId, String userHandle) {
        ProblemEntity problem = problemRepository.findByProblemId(problemId).orElse(null);
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
                ProblemDetailDTO.of(problem),
                ProblemBodyDTO.of(body, samples)
        );
    }
}
