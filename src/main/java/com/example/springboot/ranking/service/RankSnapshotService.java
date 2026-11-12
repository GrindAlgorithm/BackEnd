package com.example.springboot.ranking.service;

import com.example.springboot.ranking.dto.RankHistoryDTO;
import com.example.springboot.ranking.dto.RankingEntryDTO;
import com.example.springboot.ranking.entity.SeasonRankSnapshotEntity;
import com.example.springboot.ranking.repository.SeasonRankSnapshotRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 순위 이력 — 매일 자정 현재 시즌 순위표를 남기고(전날 마감 기준),
 * 7일 순위 변동(§2.4 weeklyDelta)과 프로필 순위 추이(§2.15 rankHistory)에 쓴다.
 */
@Service
@RequiredArgsConstructor
public class RankSnapshotService {

    private final SeasonRepository seasonRepository;
    private final SeasonRankSnapshotRepository snapshotRepository;
    private final RankingService rankingService;

    /**
     * 현재 시즌 순위표를 snapshotDate 마감 기준으로 저장한다. 그날 스냅샷이 이미 있으면 건너뛴다.
     * ⚠ 시즌 상태 전환(SeasonLifecycleService)보다 먼저 불러야 종료일 마감 순위가 끝나는 시즌으로 남는다.
     *
     * @return 저장한 행 수 (건너뛰면 0)
     */
    @Transactional
    public int snapshotCurrentSeason(LocalDate snapshotDate) {
        SeasonEntity season = currentSeason();
        if (season == null || snapshotRepository.existsBySeasonIdAndSnapshotDate(season.getId(), snapshotDate)) {
            return 0;
        }
        List<SeasonRankSnapshotEntity> rows = rankingService.getCurrentSeasonEntries().stream()
                .map(e -> SeasonRankSnapshotEntity.createSnapshotEntity(season.getId(), snapshotDate, e.getHandle(),
                        e.getRank(), e.getScore(), e.getTierName(), e.getTierLevel()))
                .toList();
        snapshotRepository.saveAll(rows);
        return rows.size();
    }

    /**
     * 그날 마감 순위 (handle → rank). 그날 스냅샷 자체가 없으면 null — 호출부가 추정치로 대신한다.
     * 스냅샷은 있는데 handle 이 없으면 그때는 미배치였다는 뜻이다.
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> ranksOn(Integer seasonId, LocalDate date) {
        List<SeasonRankSnapshotEntity> rows = snapshotRepository.findBySeasonIdAndSnapshotDate(seasonId, date);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Integer> map = new HashMap<>();
        rows.forEach(r -> map.put(r.getHandle(), r.getRankNo()));
        return map;
    }

    /** 현재 시즌 내 순위 추이 — 시즌이 없거나 미배치(오늘도 순위 없음, 이력도 없음)면 null */
    @Transactional(readOnly = true)
    public RankHistoryDTO historyFor(String handle, LocalDate today) {
        SeasonEntity season = currentSeason();
        if (season == null) {
            return null;
        }
        List<RankHistoryDTO.Point> points = new ArrayList<>();
        for (SeasonRankSnapshotEntity s : snapshotRepository.findBySeasonIdAndHandleOrderBySnapshotDateAsc(season.getId(), handle)) {
            if (s.getSnapshotDate().isBefore(today)) {
                points.add(new RankHistoryDTO.Point(s.getSnapshotDate(), s.getRankNo(), s.getScore()));
            }
        }
        RankingEntryDTO now = rankingService.getCurrentSeasonEntry(handle);
        if (now != null) {
            points.add(new RankHistoryDTO.Point(today, now.getRank(), now.getScore()));
        }
        if (points.isEmpty()) {
            return null;
        }
        return new RankHistoryDTO(season.getId(), season.getName(), points);
    }

    private SeasonEntity currentSeason() {
        return seasonRepository.findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT).orElse(null);
    }
}
