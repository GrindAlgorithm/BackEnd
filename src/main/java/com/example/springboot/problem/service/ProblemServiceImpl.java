package com.example.springboot.problem.service;

import com.example.springboot.problem.dto.ProblemDetailDTO;
import com.example.springboot.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;

    @Override
    public ProblemDetailDTO getProblem(String problemId) {
        return problemRepository.findByProblemId(problemId)
                .map(ProblemDetailDTO::of)
                .orElse(null);
    }
}
