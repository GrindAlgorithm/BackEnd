package com.example.springboot.ranking.service;

import com.example.springboot.common.tier.TierLevel;
import com.example.springboot.common.tier.TierName;
import com.example.springboot.friend.repository.FriendshipRepository;
import com.example.springboot.ranking.dto.RankingDTO;
import com.example.springboot.ranking.dto.RankingScope;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonEntity;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.season.repository.SeasonRepository;
import com.example.springboot.user.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 랭킹 friends 스코프 (§2.13) — 내가 추가한 친구들의 시즌 랭킹만 추리고, 내 순위는 시즌 전체에서 채운다 */
@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock private SeasonRankingRepository rankingRepository;
    @Mock private SeasonRepository seasonRepository;
    @Mock private FriendshipRepository friendshipRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks private RankingServiceImpl service;

    private SeasonEntity season;

    @BeforeEach
    void setUp() {
        season = SeasonEntity.createSeasonEntity("Season 2",
                LocalDate.now().minusDays(60), LocalDate.now().plusDays(30), SeasonStatus.CURRENT);
        ReflectionTestUtils.setField(season, "id", 2);
        when(seasonRepository.findFirstByStatusOrderByIdDesc(SeasonStatus.CURRENT))
                .thenReturn(Optional.of(season));
        // 시즌 전체 랭킹(점수 내림차순): algo_god > kim_dev > algo_lover > park_algo
        // (비로그인 테스트에선 호출되지 않으므로 lenient)
        lenient().when(rankingRepository.findBySeason_IdOrderByScoreDesc(2)).thenReturn(List.of(
                row("algo_god", 3201, 412),
                row("kim_dev", 2890, 350),
                row("algo_lover", 2433, 287),
                row("park_algo", 1980, 240)));
    }

    @Test
    void friendsScope_returnsOnlyMyFriends_keepingGlobalSeasonRank() {
        when(currentUserProvider.currentHandle()).thenReturn("algo_lover");
        when(friendshipRepository.findFriendHandlesByOwnerHandle("algo_lover"))
                .thenReturn(List.of("algo_god", "park_algo"));

        RankingDTO dto = service.getRanking(RankingScope.FRIENDS);

        // 친구(algo_god, park_algo)만 노출, 순위는 시즌 전체 기준(1위, 4위) 유지
        assertEquals(2, dto.getEntries().size());
        assertEquals("algo_god", dto.getEntries().get(0).getHandle());
        assertEquals(1, dto.getEntries().get(0).getRank());
        assertEquals("park_algo", dto.getEntries().get(1).getHandle());
        assertEquals(4, dto.getEntries().get(1).getRank());

        // 나는 보통 친구 목록에 없으므로 내 순위는 시즌 전체에서 채운다(algo_lover = 3위)
        assertNotNull(dto.getMyEntry());
        assertEquals("algo_lover", dto.getMyEntry().getHandle());
        assertEquals(3, dto.getMyEntry().getRank());
    }

    @Test
    void friendsScope_noFriends_returnsEmptyEntriesButKeepsMyEntry() {
        when(currentUserProvider.currentHandle()).thenReturn("algo_lover");
        when(friendshipRepository.findFriendHandlesByOwnerHandle("algo_lover"))
                .thenReturn(List.of());

        RankingDTO dto = service.getRanking(RankingScope.FRIENDS);

        assertTrue(dto.getEntries().isEmpty());
        assertNotNull(dto.getMyEntry());
        assertEquals("algo_lover", dto.getMyEntry().getHandle());
    }

    @Test
    void friendsScope_notLoggedIn_returnsEmptyEntriesAndNullMyEntry() {
        when(currentUserProvider.currentHandle()).thenReturn(null);

        RankingDTO dto = service.getRanking(RankingScope.FRIENDS);

        assertTrue(dto.getEntries().isEmpty());
        assertNull(dto.getMyEntry());
        // 비로그인이면 친구 조회를 시도하지 않는다
        verify(friendshipRepository, never()).findFriendHandlesByOwnerHandle(any());
    }

    private SeasonRankingEntity row(String handle, int score, int solved) {
        return SeasonRankingEntity.createSeasonRankingEntity(
                season, handle, TierName.DIAMOND, TierLevel.I, score, solved, LocalDateTime.now());
    }
}
