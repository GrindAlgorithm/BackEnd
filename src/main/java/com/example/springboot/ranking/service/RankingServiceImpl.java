package com.example.springboot.ranking.service;

import com.example.springboot.friend.repository.FriendshipRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final SeasonRankingRepository rankingRepository;
    private final SeasonRepository seasonRepository;
    private final FriendshipRepository friendshipRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public RankingDTO getRanking(RankingScope scope) {
        SeasonEntity current = seasonRepository
                .findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)
                .orElse(null);
        SeasonDTO season = current == null ? null : SeasonDTO.of(current, LocalDate.now());

        // 로그인 유저의 handle. 비로그인이면 null(계약 §2.13). friends 스코프 필터에도 쓴다.
        String myHandle = currentUserProvider.currentHandle();

        List<RankingEntryDTO> entries = switch (scope) {
            case SEASON -> rankedSeasonEntries(current);
            case OVERALL -> rankedOverallEntries();
            case FRIENDS -> friendEntries(current, myHandle);
        };

        RankingEntryDTO myEntry = myHandle == null ? null : findMe(entries, myHandle);
        // friends 목록엔 보통 내가 없으므로(나를 친구로 추가하지 않음) 내 순위는 시즌 랭킹에서 채운다
        if (myEntry == null && myHandle != null && scope == RankingScope.FRIENDS) {
            myEntry = findMe(rankedSeasonEntries(current), myHandle);
        }
        return new RankingDTO(season, entries, myEntry);
    }

    /**
     * 친구 랭킹 — 내가 추가한 친구 handle 들만 현재 시즌 랭킹에서 추려서 반환한다.
     * rank 는 시즌 전체 기준 순위를 유지한다(필터 후 재부여하지 않음). 비로그인·시즌 없음·친구 없음이면 빈 목록.
     */
    private List<RankingEntryDTO> friendEntries(SeasonEntity season, String myHandle) {
        if (season == null || myHandle == null) {
            return List.of();
        }
        List<String> friendHandles = friendshipRepository.findFriendHandlesByOwnerHandle(myHandle);
        if (friendHandles.isEmpty()) {
            return List.of();
        }
        Set<String> friends = new HashSet<>(friendHandles);
        return rankedSeasonEntries(season).stream()
                .filter(e -> friends.contains(e.getHandle()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RankingEntryDTO> getCurrentSeasonEntries() {
        return rankedSeasonEntries(seasonRepository
                .findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)
                .orElse(null));
    }

    @Override
    public RankingEntryDTO getCurrentSeasonEntry(String handle) {
        return handle == null ? null : findMe(getCurrentSeasonEntries(), handle);
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
