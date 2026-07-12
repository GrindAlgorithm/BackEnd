package com.example.springboot.submission.service;

import com.example.springboot.judge0.Judge0Execution;
import com.example.springboot.submission.dto.RunRequestDTO;

public interface RunService {

    /** 코드 실행 (예제 테스트용, 채점 아님) — 연동 문서 §2.9. 문제 없으면 null */
    public Judge0Execution run(RunRequestDTO request);
}
