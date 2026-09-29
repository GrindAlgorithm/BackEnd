package com.example.springboot.season.service;

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
                season.changeStatus(target);
                changed++;
            }
        }
        if (current == null) {
            log.warn("진행 중인 시즌이 없습니다 (기준일 {}) — 시즌 화면·랭킹이 비어 보입니다", today);
        }
        return changed;
    }
}
