package com.example.springboot.submission.service;

import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.submission.dto.RunRequestDTO;

public interface RunService {

    /**
     * 코드 실행 (예제 테스트용, 채점 아님) — 연동 문서 §2.9. 문제 없으면 null.
     * 풀이 세션이 이 유저·이 문제의 것이 아니면 400 INVALID_SOLVE_SESSION.
     */
    public Judge0Execution run(RunRequestDTO request, String userHandle);
}
