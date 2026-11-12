package com.example.springboot.season.service;

import com.example.springboot.ranking.service.RankSnapshotService;
import com.example.springboot.ranking.service.TierDecayService;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.title.service.TitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 매일 자정(한국 시간) 시즌 정비 — 시즌 날짜는 한국 기준이므로 서버 시간대(UTC)와 무관하게 Asia/Seoul 로 판단한다.
 * 기동 시에도 한 번 돌려, 자정에 서버가 꺼져 있었어도 다음 기동 때 바로잡힌다.
 * <ol>
 *   <li>순위 스냅샷 — 전날 마감 순위. 시즌 전환보다 먼저 해야 종료일 순위가 끝나는 시즌으로 남는다.
 *       (기동 시 실행분은 "기동 시점" 순위라 전날 마감과 조금 다를 수 있다 — 이미 있으면 건너뜀)</li>
 *   <li>시즌 상태 전환</li>
 *   <li>칭호 일괄 발급 — 공개 시즌 전체. 막 끝난 시즌의 챔피언 칭호도 여기서 나간다</li>
 *   <li>비활동 티어 하락 반영</li>
 * </ol>
 * 단계마다 따로 잡아, 한 단계 실패가 다음 단계·스케줄러 스레드를 막지 않게 한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeasonScheduler {

    static final String ZONE = "Asia/Seoul";

    private final SeasonLifecycleService seasonLifecycleService;
    private final RankSnapshotService rankSnapshotService;
    private final TitleService titleService;
    private final TierDecayService tierDecayService;
    private final SeasonRepository seasonRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        runDaily();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = ZONE)
    public void atMidnight() {
        runDaily();
    }

    void runDaily() {
        LocalDate today = LocalDate.now(ZoneId.of(ZONE));
        step("순위 스냅샷", () -> {
            int saved = rankSnapshotService.snapshotCurrentSeason(today.minusDays(1));
            if (saved > 0) {
                log.info("순위 스냅샷 — {} 마감 {}명", today.minusDays(1), saved);
            }
        });
        step("시즌 상태 동기화", () -> {
            int changed = seasonLifecycleService.syncStatuses(today);
            if (changed > 0) {
                log.info("시즌 상태 동기화 — {}개 시즌 전환", changed);
            }
        });
        step("칭호 일괄 발급", () -> {
            int issued = 0;
            for (SeasonEntity season : seasonRepository.findAllByOrderByIdDesc()) {
                if (season.getStatus().isPublic()) {
                    issued += titleService.awardSeason(season.getId());
                }
            }
            if (issued > 0) {
                log.info("칭호 일괄 발급 — {}개", issued);
            }
        });
        step("티어 하락 반영", () -> {
            int changed = tierDecayService.applyCurrentSeason(today);
            if (changed > 0) {
                log.info("티어 하락 반영 — {}명 티어 변경", changed);
            }
        });
    }

    private void step(String name, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            // 실패가 기동·스케줄러 스레드를 죽이지 않게 한다 — 다음 자정/기동 때 재시도
            log.error("{} 실패", name, e);
        }
    }
}
