package com.example.springboot.season.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 시즌 자동 전환 트리거. 시즌 날짜는 한국 기준이므로 서버 시간대(UTC)와 무관하게 Asia/Seoul 로 판단한다.
 * 기동 시에도 한 번 돌려, 자정에 서버가 꺼져 있었어도 다음 기동 때 바로잡힌다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeasonScheduler {

    static final String ZONE = "Asia/Seoul";

    private final SeasonLifecycleService seasonLifecycleService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        sync();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = ZONE)
    public void atMidnight() {
        sync();
    }

    private void sync() {
        try {
            int changed = seasonLifecycleService.syncStatuses(LocalDate.now(ZoneId.of(ZONE)));
            if (changed > 0) {
                log.info("시즌 상태 동기화 — {}개 시즌 전환", changed);
            }
        } catch (Exception e) {
            // 전환 실패가 기동·스케줄러 스레드를 죽이지 않게 한다 — 다음 자정/기동 때 재시도
            log.error("시즌 상태 동기화 실패", e);
        }
    }
}
