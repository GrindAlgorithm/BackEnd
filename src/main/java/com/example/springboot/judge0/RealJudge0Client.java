package com.example.springboot.judge0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 실제 Judge0 HTTP 클라이언트 (연동 문서 §3). base64_encoded=true.
 * expected_output 을 함께 보내면 Judge0가 정답 비교까지 수행한다.
 *
 * 두 가지 호출 방식을 설정으로 지원한다:
 * - wait=true (self-host 기본): POST 한 번으로 결과까지 받는 동기 호출.
 * - wait=false (공용/RapidAPI): POST 로 토큰만 받고 GET /submissions/{token} 폴링.
 *   공용 인스턴스는 wait=true 를 거부하는 경우가 많다.
 * 인증은 authToken(X-Auth-Token) + headers(임의 헤더, RapidAPI 키 등) 로 구성한다.
 */
@Component
@ConditionalOnProperty(name = "judge0.client", havingValue = "real")
@Slf4j
public class RealJudge0Client implements Judge0Client {

    private final RestClient restClient;
    private final Judge0Properties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RealJudge0Client(Judge0Properties props) {
        this.props = props;
        // Judge0 서버는 chunked 전송 인코딩의 요청 본문을 읽지 못해 본문을 빈 값으로
        // 인식(422 source_code blank)한다. 두 가지를 함께 적용해 chunked 를 없앤다:
        // 1) HttpURLConnection 팩토리로 JDK HttpClient 의 h2c 업그레이드를 회피.
        // 2) 본문을 byte[] 로 미리 직렬화해 전달 → Content-Length 가 설정되어
        //    HttpURLConnection 이 fixed-length 모드로 전송(chunked 아님, curl 과 동일).
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(requestFactory);
        if (props.getAuthToken() != null && !props.getAuthToken().isBlank()) {
            builder.defaultHeader("X-Auth-Token", props.getAuthToken());
        }
        props.getHeaders().forEach((name, value) -> {
            if (value != null && !value.isBlank()) {
                builder.defaultHeader(name, value);
            }
        });
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

        byte[] payload = toJson(body);
        Judge0Response res = props.isWait() ? submitAndWait(payload) : submitAndPoll(payload);

        if (res == null || res.status == null) {
            log.warn("[judge0] 빈 응답 — INTERNAL_ERROR 처리");
            return new Judge0Execution(Judge0Verdict.INTERNAL_ERROR, null, null, null, null, null);
        }

        Judge0Verdict verdict = Judge0Verdict.fromStatusId(res.status.id);
        Long timeMs = res.time == null ? null : Math.round(Double.parseDouble(res.time) * 1000);
        String stderr = res.stderr != null ? decode(res.stderr) : decode(res.compileOutput);
        return new Judge0Execution(verdict, decode(res.stdout), stderr, timeMs, res.memory, res.exitCode);
    }

    /** Map → JSON byte[] (Content-Length 설정을 위해 미리 직렬화). */
    private byte[] toJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Judge0 요청 직렬화 실패", e);
        }
    }

    /** wait=true: POST 한 번으로 완료 결과를 받는다. */
    private Judge0Response submitAndWait(byte[] payload) {
        return restClient.post()
                .uri(uri -> uri.path("/submissions")
                        .queryParam("base64_encoded", true)
                        .queryParam("wait", true)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Judge0Response.class);
    }

    /** wait=false: 토큰을 받은 뒤 status.id 가 큐/처리중(1,2)을 벗어날 때까지 GET 폴링. */
    private Judge0Response submitAndPoll(byte[] payload) {
        Judge0Response created = restClient.post()
                .uri(uri -> uri.path("/submissions")
                        .queryParam("base64_encoded", true)
                        .queryParam("wait", false)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Judge0Response.class);

        if (created == null || created.token == null) {
            log.warn("[judge0] 제출 토큰 없음 — INTERNAL_ERROR 처리");
            return null;
        }
        String token = created.token;

        for (int i = 0; i < props.getPollMaxTries(); i++) {
            Judge0Response res = restClient.get()
                    .uri(uri -> uri.path("/submissions/{token}")
                            .queryParam("base64_encoded", true)
                            .build(token))
                    .retrieve()
                    .body(Judge0Response.class);
            // status.id 1=In Queue, 2=Processing → 아직 채점 중
            if (res != null && res.status != null && res.status.id > 2) {
                return res;
            }
            try {
                Thread.sleep(props.getPollIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[judge0] 폴링 중단 token={}", token);
                return null;
            }
        }
        log.warn("[judge0] 폴링 타임아웃 token={} ({}회 초과)", token, props.getPollMaxTries());
        return null;
    }

    private String encode(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String b64) {
        if (b64 == null) {
            return null;
        }
        // Judge0 는 base64 응답에 개행을 섞어 보낸다(예: "MQo=\n"). 표준 디코더는 개행을
        // 거부하므로, 공백/개행을 무시하는 MIME 디코더를 쓴다.
        return new String(Base64.getMimeDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Judge0Response {
        public String token; // wait=false 제출 응답에 포함
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
