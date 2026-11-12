package com.example.springboot.ranking.service;

import com.example.springboot.common.tier.TierDecay;
import com.example.springboot.dashboard.dto.DecayWarningDTO;
import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 비활동 티어 하락 (A1) — 규칙은 {@link TierDecay}.
 * 하락 반영은 매일 자정 스케줄러(SeasonScheduler)가, 경고는 대시보드·프로필 조회 시 계산한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TierDecayService {

    private final SeasonRankingRepository rankingRepository;
    private final SubmissionRepository submissionRepository;

    /**
     * 현재 시즌 랭킹 전체에 하락을 반영한다. 점수 기준으로 매번 다시 계산하므로 여러 번 돌려도 같다.
     *
     * @return 티어가 바뀐 행 수
     */
    @Transactional
    public int applyCurrentSeason(LocalDate today) {
        List<SeasonRankingEntity> rows = rankingRepository.findBySeason_StatusOrderByScoreDesc(SeasonStatus.CURRENT);
        if (rows.isEmpty()) {
            return 0;
        }
        Map<String, LocalDateTime> lastAccepted = lastAcceptedAt(rows.stream().map(SeasonRankingEntity::getHandle).toList());
        int changed = 0;
        for (SeasonRankingEntity row : rows) {
            int inactive = inactiveDays(row, lastAccepted.get(row.getHandle()), today);
            TierDecay.TierRank tier = TierDecay.effectiveTier(row.getScore(), inactive);
            if (row.changeTier(tier.name(), tier.level())) {
                changed++;
                log.info("티어 하락 반영 handle={} score={} 비활동 {}일 → {} {}",
                        row.getHandle(), row.getScore(), inactive, tier.name(), tier.level());
            }
        }
        return changed;
    }

    /** 현재 시즌 하락 예고 — 미배치거나 곧 하락할 일이 없으면 null (§2.4·§2.15 decay) */
    @Transactional(readOnly = true)
    public DecayWarningDTO warningFor(String handle, LocalDate today) {
        if (handle == null) {
            return null;
        }
        SeasonRankingEntity row = rankingRepository.findFirstBySeason_StatusAndHandle(SeasonStatus.CURRENT, handle)
                .orElse(null);
        if (row == null) {
            return null;
        }
        LocalDateTime last = lastAcceptedAt(List.of(handle)).get(handle);
        TierDecay.Warning w = TierDecay.warning(row.getScore(), inactiveDays(row, last, today));
        if (w == null) {
            return null;
        }
        return new DecayWarningDTO(w.inactiveDays(), w.daysUntilDrop(),
                TierRankDTO.of(w.fromTier().name(), w.fromTier().level()),
                TierRankDTO.of(w.toTier().name(), w.toTier().level()));
    }

    /** 마지막 활동일(랭킹 행의 마지막 첫 정답, 아무 문제 정답 중 늦은 쪽)부터 오늘까지 일수 */
    static int inactiveDays(SeasonRankingEntity row, LocalDateTime lastAccepted, LocalDate today) {
        LocalDateTime last = row.getLastActiveAt();
        if (lastAccepted != null && (last == null || lastAccepted.isAfter(last))) {
            last = lastAccepted;
        }
        if (last == null) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.DAYS.between(last.toLocalDate(), today));
    }

    private Map<String, LocalDateTime> lastAcceptedAt(List<String> handles) {
        Map<String, LocalDateTime> map = new HashMap<>();
        for (Object[] r : submissionRepository.lastSubmittedAtByHandles(handles, SubmissionStatus.ACCEPTED)) {
            map.put((String) r[0], (LocalDateTime) r[1]);
        }
        return map;
    }
}
