package com.example.springboot.integrity.service;

import com.example.springboot.integrity.dto.SolveEventBatchRequestDTO;
import com.example.springboot.integrity.dto.SolveEventRequestDTO;
import com.example.springboot.integrity.dto.SolveIntegritySummaryRequestDTO;
import com.example.springboot.integrity.entity.SolveEventEntity;
import com.example.springboot.integrity.entity.SolveSessionSummaryEntity;
import com.example.springboot.integrity.repository.SolveEventRepository;
import com.example.springboot.integrity.repository.SolveSessionSummaryRepository;
import com.example.springboot.problem.repository.SolveSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IntegrityServiceImpl implements IntegrityService {

    /** 배치당 이벤트 상한 — 프론트 전송 큐 상한(200건, §2.17)과 동일 */
    private static final int MAX_BATCH_EVENTS = 200;

    /** 세션당 누적 적재 상한 — 남용 방지(§2.17 서버 측 권장 처리 5) */
    private static final long MAX_SESSION_EVENTS = 2_000;

    private final SolveEventRepository solveEventRepository;
    private final SolveSessionSummaryRepository summaryRepository;
    private final SolveSessionRepository solveSessionRepository;

    @Override
    public int saveBatch(String solveSessionId, SolveEventBatchRequestDTO request) {
        // 세션 검증 — 없는 세션의 배치는 버린다 (DB 초기화 등으로 세션이 사라진 클라이언트의 무한 재시도 방지)
        if (!solveSessionRepository.existsById(solveSessionId)) {
            log.warn("saveBatch: 알 수 없는 풀이 세션 — 배치 폐기 solveSessionId={}, events={}",
                    solveSessionId, request.getEvents() == null ? 0 : request.getEvents().size());
            return 0;
        }

        LocalDateTime receivedAt = LocalDateTime.now();
        int saved = saveEvents(solveSessionId, request, receivedAt);
        upsertSummary(solveSessionId, request.getSummary(), receivedAt);
        return saved;
    }

    private int saveEvents(String solveSessionId, SolveEventBatchRequestDTO request, LocalDateTime receivedAt) {
        List<SolveEventRequestDTO> events = request.getEvents();
        if (events == null || events.isEmpty()) {
            return 0;
        }

        // 상한 초과분은 잘라내고 로그만 남긴다 — 신호는 참고 지표라 유실을 허용
        long stored = solveEventRepository.countBySolveSessionId(solveSessionId);
        int capacity = (int) Math.max(0, Math.min(MAX_BATCH_EVENTS, MAX_SESSION_EVENTS - stored));
        if (events.size() > capacity) {
            log.warn("saveBatch: 이벤트 상한 초과 — {}건 중 {}건만 적재 solveSessionId={}",
                    events.size(), capacity, solveSessionId);
            events = events.subList(0, capacity);
        }

        List<SolveEventEntity> entities = new ArrayList<>(events.size());
        for (SolveEventRequestDTO event : events) {
            SolveEventRequestDTO.DetailDTO detail = event.getDetail();
            entities.add(SolveEventEntity.createSolveEventEntity(
                    solveSessionId,
                    request.getProblemId(),
                    event.getType(),
                    event.getSeverity(),
                    parseClientTime(event.getAt()),
                    event.getMessage(),
                    detail == null ? null : detail.getChars(),
                    detail == null ? null : detail.getDurationMs(),
                    detail == null ? null : detail.getCps(),
                    detail == null ? null : detail.getCv(),
                    detail == null ? null : detail.getLine(),
                    receivedAt));
        }
        solveEventRepository.saveAll(entities);
        return entities.size();
    }

    private void upsertSummary(String solveSessionId, SolveIntegritySummaryRequestDTO summary,
                               LocalDateTime receivedAt) {
        if (summary == null) {
            return;
        }
        SolveSessionSummaryEntity entity = summaryRepository.findById(solveSessionId)
                .orElseGet(() -> SolveSessionSummaryEntity.createSolveSessionSummaryEntity(solveSessionId));
        entity.update(summary.getRiskScore(), summary.getLevel(), summary.getTypedChars(),
                summary.getInsertedChars(), summary.getInternalPasteChars(), summary.getFinalCodeChars(),
                summary.getAuthorshipRatio(), summary.getActiveMs(), summary.getBlurredMs(),
                summary.getBlurCount(), receivedAt);
        summaryRepository.save(entity);
    }

    /** 클라이언트 시계(ISO 8601) 파싱 — 실패하면 null (received_at 으로 대조) */
    private LocalDateTime parseClientTime(String at) {
        if (at == null || at.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(at).toLocalDateTime();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
