package com.example.springboot.problem.service;

import com.example.springboot.problem.dto.OpenProblemDTO;
import com.example.springboot.problem.dto.ProblemDetailDTO;

public interface ProblemService {

    /** 문제 상세 (본문·예제 제외, B2) — 연동 문서 §2.7. 없으면 null */
    public ProblemDetailDTO getProblem(String problemId);

    /**
     * IDE 진입 = 본문 열람 (연동 문서 §2.8).
     * 풀이 세션을 발급하고 열람 시각(풀이 시작 시각, B2)을 기록한 뒤 본문과 함께 반환한다.
     * 문제가 없으면 null.
     */
    public OpenProblemDTO openProblem(String problemId, String userHandle);
}
