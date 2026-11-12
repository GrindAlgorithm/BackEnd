package com.example.springboot.title.service;

import com.example.springboot.common.tier.TierDecay;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonRewardEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.season.repository.SeasonRewardRepository;
import com.example.springboot.season.service.RewardEvaluator;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.title.entity.UserTitleEntity;
import com.example.springboot.title.repository.UserTitleRepository;
import com.example.springboot.user.dto.TitleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 칭호 (A4) — 칭호 정의는 시즌 리워드(season_reward), 보유는 user_title.
 * <p>
 * 발급 조건: 리워드 조건 달성({@link RewardEvaluator}) + 그 시즌 점수 기준 티어가 골드 이상.
 * 골드 도달 전에 달성한 리워드는 골드가 되는 순간 한꺼번에 발급된다. 한 번 받은 칭호는 회수하지 않는다.
 * <p>
 * 발급 시점: 정답 채점 직후(그 유저·그 시즌) + 매일 자정 전 시즌 일괄(챔피언은 시즌 종료 후 일괄에서만).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TitleService {

    private final SeasonRepository seasonRepository;
    private final SeasonRewardRepository seasonRewardRepository;
    private final SeasonRankingRepository rankingRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserTitleRepository userTitleRepository;

    /** 시즌 전체 유저 일괄 발급. @return 새로 발급한 칭호 수 */
    @Transactional
    public int awardSeason(Integer seasonId) {
        SeasonEntity season = seasonRepository.findById(seasonId).orElse(null);
        if (season == null || !season.getStatus().isPublic()) {
            return 0;
        }
        List<SeasonRewardEntity> rewards = seasonRewardRepository.findBySeason_IdOrderBySortOrderAsc(seasonId);
        if (rewards.isEmpty()) {
            return 0;
        }
        Map<String, Set<String>> clearedByHandle = new HashMap<>();
        for (SubmissionEntity s : submissionRepository.findByProblem_Season_IdAndStatus(seasonId, SubmissionStatus.ACCEPTED)) {
            clearedByHandle.computeIfAbsent(s.getUserHandle(), h -> new HashSet<>()).add(s.getProblem().getProblemId());
        }
        int total = problemRepository.findBySeason_IdOrderByDisplayNoAsc(seasonId).size();
        List<SeasonRankingEntity> rows = rankingRepository.findBySeason_IdOrderByScoreDesc(seasonId);
        int issued = 0;
        for (int i = 0; i < rows.size(); i++) {
            SeasonRankingEntity row = rows.get(i);
            int cleared = clearedByHandle.getOrDefault(row.getHandle(), Set.of()).size();
            issued += award(season, rewards, row, i + 1, cleared, total);
        }
        return issued;
    }

    /** 한 유저만 발급 — 정답 채점 직후. @return 새로 발급한 칭호 수 */
    @Transactional
    public int awardUser(String handle, Integer seasonId) {
        SeasonEntity season = seasonRepository.findById(seasonId).orElse(null);
        if (handle == null || season == null || !season.getStatus().isPublic()) {
            return 0;
        }
        List<SeasonRankingEntity> rows = rankingRepository.findBySeason_IdOrderByScoreDesc(seasonId);
        for (int i = 0; i < rows.size(); i++) {
            if (handle.equals(rows.get(i).getHandle())) {
                int cleared = (int) submissionRepository.findByProblem_Season_IdAndUserHandle(seasonId, handle).stream()
                        .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                        .map(s -> s.getProblem().getProblemId())
                        .distinct()
                        .count();
                int total = problemRepository.findBySeason_IdOrderByDisplayNoAsc(seasonId).size();
                return award(season, seasonRewardRepository.findBySeason_IdOrderBySortOrderAsc(seasonId),
                        rows.get(i), i + 1, cleared, total);
            }
        }
        return 0; // 미배치
    }

    private int award(SeasonEntity season, List<SeasonRewardEntity> rewards, SeasonRankingEntity row,
                      int rank, int cleared, int total) {
        if (!TierDecay.isGoldOrAbove(row.getScore())) {
            return 0; // A4: 골드 이상부터 발급
        }
        RewardEvaluator.Progress progress = new RewardEvaluator.Progress(rank, row.getScore(), row.getSolvedCount(),
                cleared, total, season.getStatus() == SeasonStatus.PAST);
        int issued = 0;
        LocalDateTime now = LocalDateTime.now();
        for (SeasonRewardEntity reward : rewards) {
            if (RewardEvaluator.evaluate(reward, progress).achieved()
                    && userTitleRepository.insertIfAbsent(row.getHandle(), reward.getId(), now) > 0) {
                issued++;
                log.info("칭호 발급 handle={} titleId={}", row.getHandle(), reward.getId());
            }
        }
        return issued;
    }

    /**
     * 프로필 칭호 목록 (§2.15 titles) — 공개 시즌의 리워드를 최신 시즌부터.
     * 본인이면 보유+미보유 전체, 타인이면 보유만. expired = 종료된 시즌의 미보유 칭호.
     */
    @Transactional(readOnly = true)
    public List<TitleResponseDTO> titlesFor(String handle, boolean isMe) {
        Set<String> owned = userTitleRepository.findByHandle(handle).stream()
                .map(UserTitleEntity::getTitleId)
                .collect(Collectors.toSet());
        List<TitleResponseDTO> titles = new ArrayList<>();
        for (SeasonEntity season : seasonRepository.findAllByOrderByIdDesc()) {
            if (!season.getStatus().isPublic()) {
                continue;
            }
            for (SeasonRewardEntity r : seasonRewardRepository.findBySeason_IdOrderBySortOrderAsc(season.getId())) {
                boolean has = owned.contains(r.getId());
                if (!has && !isMe) {
                    continue;
                }
                titles.add(new TitleResponseDTO(r.getId(), r.getName(), r.getConditionText(), r.getColorKey(),
                        has, season.getId(), !has && season.getStatus() == SeasonStatus.PAST));
            }
        }
        return titles;
    }

    @Transactional(readOnly = true)
    public boolean owns(String handle, String titleId) {
        return userTitleRepository.existsByHandleAndTitleId(handle, titleId);
    }
}
