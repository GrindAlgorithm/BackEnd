package com.example.springboot.problem.service;

import com.example.springboot.problem.dto.ProblemDetailDTO;

public interface ProblemService {

    /** 문제 상세 (본문·예제 제외, B2) — 연동 문서 §2.7. 없으면 null */
    public ProblemDetailDTO getProblem(String problemId);
}
