package com.example.springboot.season.service;

import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/** 날짜 기준 시즌 전환 — S2 마지막 날 / S3 첫날 경계, BETA 불변, 기간 겹침 */
@ExtendWith(MockitoExtension.class)
class SeasonLifecycleServiceTest {

    @Mock private SeasonRepository seasonRepository;
    @Mock private com.example.springboot.ranking.repository.SeasonRankingRepository seasonRankingRepository;
    @Mock private com.example.springboot.notification.service.NotificationService notificationService;

    @InjectMocks private SeasonLifecycleService service;

    private SeasonEntity beta;
    private SeasonEntity s1;
    private SeasonEntity s2;
    private SeasonEntity s3;

    @BeforeEach
    void setUp() {
        beta = season("Beta", "2026-01-01", "2026-12-31", SeasonStatus.BETA);
        s1 = season("Season 1", "2026-04-01", "2026-06-30", SeasonStatus.PAST);
        s2 = season("Season 2", "2026-07-01", "2026-09-30", SeasonStatus.CURRENT);
        s3 = season("Season 3", "2026-10-01", "2026-12-31", SeasonStatus.UPCOMING);
        when(seasonRepository.findAllByOrderByIdDesc()).thenReturn(List.of(s3, s2, s1, beta));
    }

    private SeasonEntity season(String name, String start, String end, SeasonStatus status) {
        return SeasonEntity.createSeasonEntity(name, LocalDate.parse(start), LocalDate.parse(end), status);
    }

    @Test
    void lastDayOfSeason2ChangesNothing() {
        assertEquals(0, service.syncStatuses(LocalDate.parse("2026-09-30")));
        assertEquals(SeasonStatus.CURRENT, s2.getStatus());
        assertEquals(SeasonStatus.UPCOMING, s3.getStatus());
    }

    @Test
    void firstDayOfSeason3SwitchesSeasons() {
        assertEquals(2, service.syncStatuses(LocalDate.parse("2026-10-01")));
        assertEquals(SeasonStatus.PAST, s1.getStatus());
        assertEquals(SeasonStatus.PAST, s2.getStatus());
        assertEquals(SeasonStatus.CURRENT, s3.getStatus());
        assertEquals(SeasonStatus.BETA, beta.getStatus());
    }

    @Test
    void seasonInsertedWithWrongStatusIsCorrected() {
        s3.changeStatus(SeasonStatus.PAST); // 시작 전인데 PAST 로 잘못 넣음 → 공개 전으로 숨김
        service.syncStatuses(LocalDate.parse("2026-09-29"));
        assertEquals(SeasonStatus.UPCOMING, s3.getStatus());
    }

    @Test
    void gapBetweenSeasonsLeavesNoCurrent() {
        SeasonEntity s4 = season("Season 4", "2027-02-01", "2027-04-30", SeasonStatus.UPCOMING);
        when(seasonRepository.findAllByOrderByIdDesc()).thenReturn(List.of(s4, s3));
        service.syncStatuses(LocalDate.parse("2027-01-15"));
        assertEquals(SeasonStatus.PAST, s3.getStatus());
        assertEquals(SeasonStatus.UPCOMING, s4.getStatus());
    }

    @Test
    void overlappingSeasonsKeepOnlyLatestStartAsCurrent() {
        SeasonEntity early = season("Early S3", "2026-09-25", "2026-12-31", SeasonStatus.UPCOMING);
        when(seasonRepository.findAllByOrderByIdDesc()).thenReturn(List.of(early, s2));
        service.syncStatuses(LocalDate.parse("2026-09-28"));
        assertEquals(SeasonStatus.CURRENT, early.getStatus());
        assertEquals(SeasonStatus.PAST, s2.getStatus());
    }

    @Test
    void seasonEndNotifiesParticipantsFinalRank() {
        org.springframework.test.util.ReflectionTestUtils.setField(s2, "id", 2);
        // 점수 내림차순: god(1위), kim(2위)
        when(seasonRankingRepository.findBySeason_IdOrderByScoreDesc(2)).thenReturn(List.of(
                ranking("algo_god", 3000), ranking("kim_dev", 2000)));

        service.syncStatuses(LocalDate.parse("2026-10-01")); // s2 CURRENT → PAST

        org.mockito.Mockito.verify(notificationService).notify(
                org.mockito.ArgumentMatchers.eq("algo_god"),
                org.mockito.ArgumentMatchers.eq(com.example.springboot.notification.entity.NotificationType.SEASON_ENDED),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.contains("최종 1위"));
        org.mockito.Mockito.verify(notificationService).notify(
                org.mockito.ArgumentMatchers.eq("kim_dev"),
                org.mockito.ArgumentMatchers.eq(com.example.springboot.notification.entity.NotificationType.SEASON_ENDED),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.contains("최종 2위"));
    }

    private com.example.springboot.ranking.entity.SeasonRankingEntity ranking(String handle, int score) {
        return com.example.springboot.ranking.entity.SeasonRankingEntity.createSeasonRankingEntity(
                s2, handle, com.example.springboot.common.tier.TierName.GOLD,
                com.example.springboot.common.tier.TierLevel.I, score, 1, LocalDate.parse("2026-09-30").atStartOfDay());
    }
}
