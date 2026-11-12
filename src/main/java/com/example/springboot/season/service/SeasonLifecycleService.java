package com.example.springboot.season.service;

import com.example.springboot.notification.entity.NotificationType;
import com.example.springboot.notification.service.NotificationService;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * 시즌 상태를 날짜에 맞춘다 — 끝난 시즌은 PAST, 기간 안의 시즌은 CURRENT, 시작 전 시즌은 UPCOMING.
 * BETA 는 운영자가 정한 특수 시즌이라 건드리지 않는다.
 * 호출: SeasonScheduler (서버 기동 시 + 매일 한국 시간 자정).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonLifecycleService {

    private final SeasonRepository seasonRepository;
    private final SeasonRankingRepository seasonRankingRepository;
    private final NotificationService notificationService;

    /** @return 상태가 바뀐 시즌 수 */
    @Transactional
    public int syncStatuses(LocalDate today) {
        List<SeasonEntity> seasons = seasonRepository.findAllByOrderByIdDesc().stream()
                .filter(s -> s.getStatus() != SeasonStatus.BETA)
                .toList();

        // 기간이 겹치게 잘못 입력돼도 CURRENT 는 하나만 — 가장 늦게 시작한 시즌
        SeasonEntity current = seasons.stream()
                .filter(s -> !today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate()))
                .max(Comparator.comparing(SeasonEntity::getStartDate))
                .orElse(null);

        int changed = 0;
        for (SeasonEntity season : seasons) {
            SeasonStatus target;
            if (season == current) {
                target = SeasonStatus.CURRENT;
            } else if (today.isBefore(season.getStartDate())) {
                target = SeasonStatus.UPCOMING;
            } else {
                target = SeasonStatus.PAST; // 종료됐거나, 겹친 기간에서 밀려난 시즌
            }
            if (season.getStatus() != target) {
                log.info("시즌 상태 전환 seasonId={} name={} {} → {} (기준일 {})",
                        season.getId(), season.getName(), season.getStatus(), target, today);
                boolean ended = season.getStatus() == SeasonStatus.CURRENT && target == SeasonStatus.PAST;
                season.changeStatus(target);
                changed++;
                if (ended) {
                    notifyFinalRanks(season); // 막 끝난 시즌 — 참가자에게 최종 순위 안내
                }
            }
        }
        if (current == null) {
            log.warn("진행 중인 시즌이 없습니다 (기준일 {}) — 시즌 화면·랭킹이 비어 보입니다", today);
        }
        return changed;
    }

    /**
     * 막 끝난 시즌의 참가자에게 최종 순위를 알린다. 점수 내림차순으로 순위를 매겨
     * 각 handle 에게 "시즌 N 종료 — 최종 X위" 알림을 남긴다. 참가자가 없으면 아무것도 하지 않는다.
     */
    private void notifyFinalRanks(SeasonEntity season) {
        List<SeasonRankingEntity> ranked = seasonRankingRepository.findBySeason_IdOrderByScoreDesc(season.getId());
        for (int i = 0; i < ranked.size(); i++) {
            int rank = i + 1;
            String handle = ranked.get(i).getHandle();
            notificationService.notify(handle, NotificationType.SEASON_ENDED, null, (long) season.getId(),
                    season.getName() + " 종료 — 최종 " + rank + "위입니다");
        }
        if (log.isInfoEnabled()) {
            log.info("시즌 종료 알림 seasonId={} 참가자={}명", season.getId(), ranked.size());
        }
    }
}
