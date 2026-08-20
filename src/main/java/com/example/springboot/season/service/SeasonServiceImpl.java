package com.example.springboot.season.service;

import com.example.springboot.common.tier.TierName;
import com.example.springboot.problem.dto.ProblemSummaryDTO;
import com.example.springboot.problem.entity.ProblemStatus;
import com.example.springboot.problem.repository.ProblemRepository;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.dto.PastSeasonDTO;
import com.example.springboot.season.dto.SeasonDTO;
import com.example.springboot.season.dto.SeasonDetailDTO;
import com.example.springboot.season.dto.SeasonRewardDTO;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonRewardEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.season.repository.SeasonRewardRepository;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final ProblemRepository problemRepository;
    private final SeasonRewardRepository seasonRewardRepository;
    private final SeasonRankingRepository seasonRankingRepository;
    private final SubmissionRepository submissionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<SeasonDTO> getSeasons() {
        LocalDate today = LocalDate.now();
        return seasonRepository.findAllByOrderByIdDesc().stream()
                .map(season -> SeasonDTO.of(season, today))
                .toList();
    }

    @Override
    public SeasonDTO getSeason(Integer seasonId) {
        return seasonRepository.findById(seasonId)
                .map(season -> SeasonDTO.of(season, LocalDate.now()))
                .orElse(null);
    }

    @Override
    public List<ProblemSummaryDTO> getSeasonProblems(Integer seasonId) {
        return problemRepository.findBySeason_IdOrderByDisplayNoAsc(seasonId).stream()
                .map(ProblemSummaryDTO::of)
                .toList();
    }

    @Override
    public SeasonDetailDTO getCurrentSeasonDetail() {
        LocalDate today = LocalDate.now();
        SeasonEntity current = seasonRepository
                .findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)
                .orElse(null);
        if (current == null) {
            return null;
        }

        String myHandle = currentUserProvider.currentHandle();

        // 내 제출 이력 → 문제별 진행 상태 + 클리어 수 (미로그인이면 전부 untried)
        Map<String, ProblemStatus> myStatuses = myProblemStatuses(current.getId(), myHandle);
        int clearedCount = (int) myStatuses.values().stream()
                .filter(status -> status == ProblemStatus.CLEARED)
                .count();

        List<ProblemSummaryDTO> problems = problemRepository
                .findBySeason_IdOrderByDisplayNoAsc(current.getId()).stream()
                .map(p -> ProblemSummaryDTO.of(p, myStatuses.getOrDefault(p.getProblemId(), ProblemStatus.UNTRIED)))
                .toList();

        // 시즌 랭킹에서 내 순위/티어/시즌 풀이 수 (리워드 판정 입력)
        Integer myRank = null;
        SeasonRankingEntity myRow = null;
        List<SeasonRankingEntity> rankings = seasonRankingRepository.findBySeason_IdOrderByScoreDesc(current.getId());
        for (int i = 0; i < rankings.size(); i++) {
            if (rankings.get(i).getHandle().equals(myHandle)) {
                myRank = i + 1;
                myRow = rankings.get(i);
                break;
            }
        }

        final Integer rank = myRank;
        final SeasonRankingEntity row = myRow;
        List<SeasonRewardDTO> rewards = seasonRewardRepository
                .findBySeason_IdOrderBySortOrderAsc(current.getId()).stream()
                .map(reward -> buildReward(reward, rank, row, clearedCount, problems.size()))
                .toList();

        return new SeasonDetailDTO(
                SeasonDTO.of(current, today),
                elapsedRatio(current, today),
                problems,
                rewards,
                buildPastSeasons()
        );
    }

    /** 유저의 시즌 내 제출 이력 → 문제별 진행 상태 (accepted 우선, 그 외 시도는 wip) */
    private Map<String, ProblemStatus> myProblemStatuses(Integer seasonId, String myHandle) {
        if (myHandle == null) {
            return Map.of();
        }
        Map<String, ProblemStatus> statuses = new HashMap<>();
        for (SubmissionEntity submission : submissionRepository.findByProblem_Season_IdAndUserHandle(seasonId, myHandle)) {
            String problemId = submission.getProblem().getProblemId();
            if (submission.getStatus() == SubmissionStatus.ACCEPTED) {
                statuses.put(problemId, ProblemStatus.CLEARED);
            } else {
                statuses.putIfAbsent(problemId, ProblemStatus.WIP);
            }
        }
        return statuses;
    }

    /**
     * 시즌 "기간 경과" 비율 0~1 (소수 둘째 자리).
     * ⚠ 대시보드 SeasonProgress.progressRatio(클리어 비율)와 다르다 — 시즌 화면 진행 바는 기간 기준.
     */
    private double elapsedRatio(SeasonEntity season, LocalDate today) {
        long totalDays = ChronoUnit.DAYS.between(season.getStartDate(), season.getEndDate());
        if (totalDays <= 0) {
            return 1.0;
        }
        double ratio = (double) ChronoUnit.DAYS.between(season.getStartDate(), today) / totalDays;
        return Math.min(1.0, Math.max(0.0, Math.round(ratio * 100) / 100.0));
    }

    /** 리워드 달성 여부/진행 문구 — 조회 유저의 랭킹·클리어 이력 기준 */
    private SeasonRewardDTO buildReward(SeasonRewardEntity reward, Integer myRank, SeasonRankingEntity myRow,
                                        int clearedCount, int totalCount) {
        boolean achieved;
        String progressText;
        switch (reward.getConditionType()) {
            case CHAMPION -> {
                // 시즌 종료 시점에 확정 — 진행 중에는 현재 순위만 노출
                achieved = false;
                progressText = myRank == null ? "진행중 (순위 없음)" : "진행중 (" + myRank + "위)";
            }
            case REACH_DIAMOND -> {
                achieved = myRow != null && myRow.getTierName() == TierName.DIAMOND;
                progressText = achieved ? "달성"
                        : myRow == null ? "진행중 (미배치)"
                        : "진행중 (" + koreanTier(myRow.getTierName()) + " " + myRow.getTierLevel().name() + ")";
            }
            case CLEAR_ALL -> {
                achieved = totalCount > 0 && clearedCount >= totalCount;
                progressText = achieved ? "달성" : "진행중 (" + clearedCount + "/" + totalCount + ")";
            }
            case CLEAR_COUNT -> {
                int need = reward.getThreshold() == null ? 1 : reward.getThreshold();
                achieved = clearedCount >= need;
                progressText = achieved ? "달성" : "진행중 (" + clearedCount + "/" + need + ")";
            }
            case SOLVE_COUNT -> {
                int need = reward.getThreshold() == null ? 0 : reward.getThreshold();
                int solved = myRow == null ? 0 : myRow.getSolvedCount();
                achieved = need > 0 && solved >= need;
                progressText = achieved ? "달성" : "진행중 (" + solved + "/" + need + ")";
            }
            default -> {
                achieved = false;
                progressText = "진행중";
            }
        }
        return new SeasonRewardDTO(reward.getId(), reward.getName(), reward.getColorKey(),
                reward.getConditionText(), achieved, progressText);
    }

    /** 지난 시즌 목록 — 챔피언은 해당 시즌 랭킹 1위. 랭킹이 없는 시즌은 표에서 제외(계약상 champion 필수) */
    private List<PastSeasonDTO> buildPastSeasons() {
        List<PastSeasonDTO> rows = new ArrayList<>();
        for (SeasonEntity season : seasonRepository.findAllByOrderByIdDesc()) {
            if (season.getStatus() == SeasonStatus.CURRENT) {
                continue;
            }
            SeasonRankingEntity champion = seasonRankingRepository
                    .findBySeason_IdOrderByScoreDesc(season.getId()).stream()
                    .findFirst()
                    .orElse(null);
            if (champion == null) {
                log.warn("buildPastSeasons: 랭킹 없는 시즌 제외 seasonId={}", season.getId());
                continue;
            }
            rows.add(new PastSeasonDTO(season.getId(), season.getName(), periodText(season),
                    champion.getHandle(), champion.getTierName(), champion.getTierLevel()));
        }
        return rows;
    }

    private String periodText(SeasonEntity season) {
        if (season.getStatus() == SeasonStatus.BETA) {
            return "베타 시즌";
        }
        return season.getStartDate().getMonthValue() + "/" + season.getStartDate().getDayOfMonth()
                + " ~ " + season.getEndDate().getMonthValue() + "/" + season.getEndDate().getDayOfMonth();
    }

    /** 티어 한글 표기 (리워드 진행 문구용) */
    private String koreanTier(TierName tierName) {
        return switch (tierName) {
            case BRONZE -> "브론즈";
            case SILVER -> "실버";
            case GOLD -> "골드";
            case PLATINUM -> "플래티넘";
            case DIAMOND -> "다이아";
        };
    }
}
