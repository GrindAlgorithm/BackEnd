package com.example.springboot.judge0;

/**
 * Judge0 채점 엔진 클라이언트 (연동 문서 §3, B3 — 항상 백엔드 뒤에 위치).
 * 구현: {@link RealJudge0Client}(HTTP) / {@link StubJudge0Client}(로컬 시뮬레이션).
 */
public interface Judge0Client {

    /** 소스를 단건 실행/채점하고 결과를 동기로 반환 (Judge0 wait=true). */
    Judge0Execution execute(Judge0ExecRequest request);
}
