package com.example.springboot.integrity.service;

import com.example.springboot.integrity.dto.SolveEventBatchRequestDTO;

public interface IntegrityService {

    /**
     * 부정행위 신호 배치 적재 — 이벤트는 append-only, 요약은 세션당 1행 갱신 (연동 문서 §2.17).
     *
     * @return 저장된 이벤트 수. 세션이 없으면 배치를 버리고 0을 반환한다
     *         (fire-and-forget 채널 — 실패 응답을 주면 프론트가 무한 재시도하므로 조용히 폐기 + 경고 로그)
     */
    public int saveBatch(String solveSessionId, SolveEventBatchRequestDTO request);
}
