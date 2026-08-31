package com.example.springboot.user.service;

import com.example.springboot.dashboard.dto.ActivityCalendarDTO;
import com.example.springboot.dashboard.dto.ActivityDayDTO;
import com.example.springboot.problem.entity.ProblemEntity;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.submission.entity.SubmissionEntity;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.dto.RecentSolvedItemDTO;
import com.example.springboot.user.dto.RecentSubmissionItemDTO;
import com.example.springboot.user.dto.UserProfileDTO;
import com.example.springboot.user.dto.UserStatsDTO;
import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService {

    /** 잔디 달력 — 최근 52주 (§2.15 activity) */
    private static final int ACTIVITY_DAYS = 364;
    private static final int RECENT_SOLVED_LIMIT = 5;
    private static final int RECENT_SUBMISSIONS_LIMIT = 10;

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final SeasonRankingRepository rankingRepository;
    private final SeasonRepository seasonRepository;

    @Override
    public UserProfileDTO getProfile(String handle, String viewerHandle) {
        UserEntity user = userRepository.findByHandle(handle).orElse(null);
        if (user == null) {
            return null;
        }

        List<SubmissionEntity> submissions =
                submissionRepository.findByUserHandleOrderBySubmittedAtDesc(handle);

        // 문제별 최초 정답 시각 — solvedCount·recentSolved·잔디 카운트의 공통 기준
        Map<ProblemEntity, LocalDateTime> firstSolvedAt = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .collect(Collectors.toMap(
                        SubmissionEntity::getProblem,
                        SubmissionEntity::getSubmittedAt,
                        (a, b) -> a.isBefore(b) ? a : b,
                        LinkedHashMap::new));

        UserProfileDTO profile = new UserProfileDTO();
        profile.setHandle(user.getHandle());
        profile.setJoinedAt(user.getJoinedAt());
        profile.setMe(handle.equals(viewerHandle));
        profile.setSelectedTitleId(user.getSelectedTitleId());
        profile.setStats(buildStats(submissions, firstSolvedAt.size()));
        profile.setActivity(buildActivity(firstSolvedAt.values()));
        profile.setRecentSolved(buildRecentSolved(firstSolvedAt));
        profile.setRecentSubmissions(submissions.stream()
                .limit(RECENT_SUBMISSIONS_LIMIT)
                .map(RecentSubmissionItemDTO::of)
                .toList());
        applySeasonRanking(profile, handle);
        return profile;
    }

    @Override
    @Transactional
    public boolean updateTitle(String email, String titleId) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }
        // 칭호 발급 도메인(A4) 구현 전 — 보유 칭호가 없으므로 해제(null)만 유효 (§2.16 400: 미보유 지정)
        if (titleId != null) {
            return false;
        }
        user.changeTitle(null);
        return true;
    }

    /** 현재 시즌 랭킹 행으로 seasonTier/score/rank 를 채운다. 미배치면 null/0/null 유지. */
    private void applySeasonRanking(UserProfileDTO profile, String handle) {
        SeasonEntity current = seasonRepository
                .findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT)
                .orElse(null);
        if (current == null) {
            return;
        }
        List<SeasonRankingEntity> rows = rankingRepository.findBySeason_IdOrderByScoreDesc(current.getId());
        for (int i = 0; i < rows.size(); i++) {
            SeasonRankingEntity row = rows.get(i);
            if (handle.equals(row.getHandle())) {
                profile.setSeasonTierName(row.getTierName());
                profile.setSeasonTierLevel(row.getTierLevel());
                profile.setSeasonScore(row.getScore());
                profile.setSeasonRank(i + 1);
                return;
            }
        }
    }

    private UserStatsDTO buildStats(List<SubmissionEntity> submissions, int solvedCount) {
        int submissionCount = submissions.size();
        long acceptedCount = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .count();
        double accuracyRate = submissionCount == 0
                ? 0.0
                : Math.round(acceptedCount * 1000.0 / submissionCount) / 10.0;
        double avgAttempts = solvedCount == 0
                ? 0.0
                : Math.round(submissionCount * 10.0 / solvedCount) / 10.0;

        // 스트릭은 "제출이 있었던 날" 기준 — 오늘 활동이 아직 없으면 어제까지 이어진 것으로 본다
        Set<LocalDate> activeDates = submissions.stream()
                .map(s -> s.getSubmittedAt().toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));
        int streakDays = currentStreak(activeDates, LocalDate.now());
        int longestStreakDays = longestStreak(activeDates);

        return new UserStatsDTO(solvedCount, submissionCount, accuracyRate, avgAttempts,
                streakDays, longestStreakDays);
    }

    private int currentStreak(Set<LocalDate> activeDates, LocalDate today) {
        LocalDate cursor = activeDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int longestStreak(Set<LocalDate> sortedDates) {
        int longest = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate d : sortedDates) {
            run = (prev != null && prev.plusDays(1).equals(d)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            prev = d;
        }
        return longest;
    }

    /** 최근 52주 잔디 — count 는 그날 처음 해결한 문제 수 (§2.4 ActivityDay 의미와 동일) */
    private ActivityCalendarDTO buildActivity(Iterable<LocalDateTime> solvedTimes) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(ACTIVITY_DAYS - 1L);

        Map<LocalDate, Integer> countByDate = new LinkedHashMap<>();
        for (LocalDateTime at : solvedTimes) {
            LocalDate d = at.toLocalDate();
            if (!d.isBefore(from) && !d.isAfter(today)) {
                countByDate.merge(d, 1, Integer::sum);
            }
        }

        List<ActivityDayDTO> days = new java.util.ArrayList<>(ACTIVITY_DAYS);
        int activeDays = 0;
        int total = 0;
        for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
            int count = countByDate.getOrDefault(d, 0);
            days.add(new ActivityDayDTO(d.toString(), count, jandiLevel(count)));
            if (count > 0) {
                activeDays++;
                total += count;
            }
        }
        double avgPerDay = activeDays == 0 ? 0.0 : Math.round(total * 10.0 / activeDays) / 10.0;
        return new ActivityCalendarDTO(days, activeDays, avgPerDay);
    }

    private int jandiLevel(int count) {
        if (count <= 0) return 0;
        if (count == 1) return 1;
        if (count == 2) return 2;
        if (count <= 4) return 3;
        return 4;
    }

    private List<RecentSolvedItemDTO> buildRecentSolved(Map<ProblemEntity, LocalDateTime> firstSolvedAt) {
        return firstSolvedAt.entrySet().stream()
                .sorted(Map.Entry.<ProblemEntity, LocalDateTime>comparingByValue(Comparator.reverseOrder()))
                .limit(RECENT_SOLVED_LIMIT)
                .map(e -> RecentSolvedItemDTO.of(e.getKey(), e.getValue()))
                .toList();
    }
}
