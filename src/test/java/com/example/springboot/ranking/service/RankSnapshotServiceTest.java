package com.example.springboot.ranking.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.notification.entity.NotificationType;
import com.example.springboot.notification.service.NotificationService;
import com.example.springboot.ranking.entity.SeasonRankSnapshotEntity;
import com.example.springboot.ranking.repository.SeasonRankSnapshotRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 순위 변동 알림 — 어제 vs 그제 스냅샷 비교, 변동 유저만, 신규 진입 제외 */
@ExtendWith(MockitoExtension.class)
class RankSnapshotServiceTest {

    @Mock private SeasonRepository seasonRepository;
    @Mock private SeasonRankSnapshotRepository snapshotRepository;
    @Mock private RankingService rankingService;
    @Mock private NotificationService notificationService;

    @InjectMocks private RankSnapshotService service;

    private final LocalDate newDate = LocalDate.parse("2026-11-11");
    private final LocalDate prevDate = LocalDate.parse("2026-11-10");
    private SeasonEntity season;

    @BeforeEach
    void setUp() {
        season = SeasonEntity.createSeasonEntity("Season 3",
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-31"), SeasonStatus.CURRENT);
        ReflectionTestUtils.setField(season, "id", 3);
        when(seasonRepository.findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT))
                .thenReturn(java.util.Optional.of(season));
    }

    @Test
    void notifiesOnlyChangedUsers_skipsNewEntrantsAndUnchanged() {
        // 어제: god 1, kim 2(상승), lee 3(신규 진입)
        when(snapshotRepository.findBySeasonIdAndSnapshotDate(3, newDate)).thenReturn(List.of(
                snap(newDate, "algo_god", 1), snap(newDate, "kim_dev", 2), snap(newDate, "lee_coder", 3)));
        // 그제: god 1(변동없음), kim 3(→2 상승), lee 없음(신규)
        when(snapshotRepository.findBySeasonIdAndSnapshotDate(3, prevDate)).thenReturn(List.of(
                snap(prevDate, "algo_god", 1), snap(prevDate, "kim_dev", 3)));

        int notified = service.notifyRankChanges(newDate, prevDate);

        assertEquals(1, notified);
        // kim 만 변동 — 3위 → 2위 상승
        verify(notificationService).notify(eq("kim_dev"), eq(NotificationType.RANK_CHANGED),
                isNull(), eq(3L), contains("상승"));
        // god(변동없음)·lee(신규)는 알림 없음
        verify(notificationService, never()).notify(eq("algo_god"), any(), any(), any(), any());
        verify(notificationService, never()).notify(eq("lee_coder"), any(), any(), any(), any());
    }

    @Test
    void noNotificationWhenPrevSnapshotMissing() {
        when(snapshotRepository.findBySeasonIdAndSnapshotDate(3, newDate)).thenReturn(List.of(
                snap(newDate, "algo_god", 1)));
        when(snapshotRepository.findBySeasonIdAndSnapshotDate(3, prevDate)).thenReturn(List.of()); // 그제 스냅샷 없음

        assertEquals(0, service.notifyRankChanges(newDate, prevDate));
        verify(notificationService, never()).notify(any(), any(), any(), any(), any());
    }

    private SeasonRankSnapshotEntity snap(LocalDate date, String handle, int rank) {
        return SeasonRankSnapshotEntity.createSnapshotEntity(3, date, handle, rank, 1000, TierName.GOLD, TierLevel.I);
    }
}
