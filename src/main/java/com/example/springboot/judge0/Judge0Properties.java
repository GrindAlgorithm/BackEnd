package com.example.springboot.judge0;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Judge0 채점 엔진 설정 (연동 문서 §3, B3).
 * client=stub(기본): 로컬 시뮬레이션(실제 Judge0 없이 파이프라인 동작).
 * client=real: 실제 Judge0(base-url)로 HTTP 호출.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {

    /** stub | real */
    private String client = "stub";

    /** Judge0 서버 주소 (client=real일 때). 절대 클라이언트에 노출 금지(B3) */
    private String baseUrl = "http://localhost:2358";

    /** Judge0 인증 토큰 (설정 시 X-Auth-Token 헤더로 전송) */
    private String authToken;

    /** 기본 CPU 시간 제한(초) — 문제별 값으로 덮어씀 */
    private float cpuTimeLimitSec = 5f;

    /** 기본 메모리 제한(KB) — 문제별 값으로 덮어씀 */
    private int memoryLimitKb = 262144;
}
