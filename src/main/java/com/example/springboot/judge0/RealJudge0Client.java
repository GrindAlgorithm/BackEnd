package com.example.springboot.judge0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 실제 Judge0 HTTP 클라이언트 (연동 문서 §3). base64_encoded=true & wait=true 동기 호출.
 * expected_output 을 함께 보내면 Judge0가 정답 비교까지 수행한다.
 */
@Component
@ConditionalOnProperty(name = "judge0.client", havingValue = "real")
@Slf4j
public class RealJudge0Client implements Judge0Client {

    private final RestClient restClient;

    public RealJudge0Client(Judge0Properties props) {
        RestClient.Builder builder = RestClient.builder().baseUrl(props.getBaseUrl());
        if (props.getAuthToken() != null && !props.getAuthToken().isBlank()) {
            builder.defaultHeader("X-Auth-Token", props.getAuthToken());
        }
        this.restClient = builder.build();
    }

    @Override
    public Judge0Execution execute(Judge0ExecRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("language_id", request.languageId());
        body.put("source_code", encode(request.sourceCode()));
        if (request.stdin() != null) {
            body.put("stdin", encode(request.stdin()));
        }
        if (request.expectedOutput() != null) {
            body.put("expected_output", encode(request.expectedOutput()));
        }
        body.put("cpu_time_limit", request.cpuTimeLimitSec());
        body.put("memory_limit", request.memoryLimitKb());

        Judge0Response res = restClient.post()
                .uri(uri -> uri.path("/submissions")
                        .queryParam("base64_encoded", true)
                        .queryParam("wait", true)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Judge0Response.class);

        if (res == null || res.status == null) {
            log.warn("[judge0] 빈 응답 — INTERNAL_ERROR 처리");
            return new Judge0Execution(Judge0Verdict.INTERNAL_ERROR, null, null, null, null, null);
        }

        Judge0Verdict verdict = Judge0Verdict.fromStatusId(res.status.id);
        Long timeMs = res.time == null ? null : Math.round(Double.parseDouble(res.time) * 1000);
        String stderr = res.stderr != null ? decode(res.stderr) : decode(res.compileOutput);
        return new Judge0Execution(verdict, decode(res.stdout), stderr, timeMs, res.memory, res.exitCode);
    }

    private String encode(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String b64) {
        if (b64 == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Judge0Response {
        public String stdout;
        public String stderr;
        @com.fasterxml.jackson.annotation.JsonProperty("compile_output")
        public String compileOutput;
        public String time; // seconds
        public Long memory; // KB
        @com.fasterxml.jackson.annotation.JsonProperty("exit_code")
        public Integer exitCode;
        public Status status;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Status {
            public int id;
            public String description;
        }
    }
}
