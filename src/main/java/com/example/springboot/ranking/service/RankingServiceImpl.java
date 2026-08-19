package com.example.springboot.ranking.service;

import com.example.springboot.ranking.dto.RankingDTO;
import com.example.springboot.ranking.dto.RankingEntryDTO;
import com.example.springboot.ranking.dto.RankingScope;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.dto.SeasonDTO;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final SeasonRankingRepository rankingRepository;
    private final SeasonRepository seasonRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public RankingDTO getRanking(RankingScope scope) {
        SeasonEntity current = seasonRepository
                .findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)
                .orElse(null);
        SeasonDTO season = current == null ? null : SeasonDTO.of(current, LocalDate.now());

        List<RankingEntryDTO> entries = switch (scope) {
            case SEASON -> rankedSeasonEntries(current);
            case OVERALL -> rankedOverallEntries();
            // 친구 관계 API 미정(연동 문서 §2.13) — 목록은 비우고 내 순위만 채운다
            case FRIENDS -> List.of();
        };

        // 로그인 유저의 handle 로 내 순위를 찾는다. 비로그인이면 null(계약 §2.13).
        String myHandle = currentUserProvider.currentHandle();
        RankingEntryDTO myEntry = myHandle == null ? null : findMe(entries, myHandle);
        if (myEntry == null && myHandle != null && scope == RankingScope.FRIENDS) {
            myEntry = findMe(rankedSeasonEntries(current), myHandle);
        }
        return new RankingDTO(season, entries, myEntry);
    }

    /** 현재 시즌 점수 내림차순 랭킹 */
    private List<RankingEntryDTO> rankedSeasonEntries(SeasonEntity season) {
        if (season == null) {
            return List.of();
        }
        List<RankingEntryDTO> entries = rankingRepository.findBySeason_IdOrderByScoreDesc(season.getId()).stream()
                .map(RankingEntryDTO::of)
                .collect(Collectors.toList());
        return assignRanks(entries);
    }

    /**
     * 전체 랭킹 — 핸들별로 모든 시즌 점수·해결 수를 합산, 티어는 최근 시즌 값.
     * ⚠ overall 정의는 스펙상 미확정(연동 문서 §2.13) — 누적 합산으로 해석.
     */
    private List<RankingEntryDTO> rankedOverallEntries() {
        Map<String, List<SeasonRankingEntity>> byHandle = rankingRepository.findAllByOrderByScoreDesc().stream()
                .collect(Collectors.groupingBy(SeasonRankingEntity::getHandle));

        List<RankingEntryDTO> entries = new ArrayList<>();
        for (Map.Entry<String, List<SeasonRankingEntity>> e : byHandle.entrySet()) {
            List<SeasonRankingEntity> rows = e.getValue();
            int totalScore = rows.stream().mapToInt(SeasonRankingEntity::getScore).sum();
            int totalSolved = rows.stream().mapToInt(SeasonRankingEntity::getSolvedCount).sum();
            LocalDateTime lastActive = rows.stream()
                    .map(SeasonRankingEntity::getLastActiveAt)
                    .max(Comparator.naturalOrder())
                    .orElseThrow();
            SeasonRankingEntity latest = rows.stream()
                    .max(Comparator.comparingInt(r -> r.getSeason().getId()))
                    .orElseThrow();
            entries.add(new RankingEntryDTO(0, e.getKey(), latest.getTierName(), latest.getTierLevel(),
                    totalScore, totalSolved, lastActive));
        }
        entries.sort(Comparator.comparingInt(RankingEntryDTO::getScore).reversed());
        return assignRanks(entries);
    }

    private List<RankingEntryDTO> assignRanks(List<RankingEntryDTO> entries) {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        return entries;
    }

    private RankingEntryDTO findMe(List<RankingEntryDTO> entries, String myHandle) {
        return entries.stream()
                .filter(e -> myHandle.equals(e.getHandle()))
                .findFirst()
                .orElse(null);
    }
}
