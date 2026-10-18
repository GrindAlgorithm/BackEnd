package com.example.springboot.friend.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.friend.dto.FriendRelation;
import com.example.springboot.friend.dto.FriendRequestDTO;
import com.example.springboot.friend.dto.FriendStatus;
import com.example.springboot.friend.dto.UserSearchItemDTO;
import com.example.springboot.friend.entity.FriendRequestEntity;
import com.example.springboot.friend.repository.FriendRequestRepository;
import com.example.springboot.friend.repository.FriendshipRepository;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 친구 신청 → 수락 (§2.18) — 에러 코드 분기·양방향 저장·검색 관계 판정 */
@ExtendWith(MockitoExtension.class)
class FriendServiceImplTest {

    @Mock private FriendshipRepository friendshipRepository;
    @Mock private FriendRequestRepository requestRepository;
    @Mock private UserRepository userRepository;
    @Mock private SeasonRankingRepository rankingRepository;
    @Mock private SubmissionRepository submissionRepository;

    @InjectMocks private FriendServiceImpl service;

    private static final LocalDateTime AT = LocalDateTime.of(2026, 10, 18, 12, 0);

    @Test
    void 자기_자신에게_신청하면_400_FRIEND_SELF() {
        ApiException e = assertThrows(ApiException.class, () -> service.sendRequest("me", "me"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("FRIEND_SELF", e.getCode());
    }

    @Test
    void 없는_핸들이면_404_USER_NOT_FOUND() {
        when(userRepository.existsByHandle("ghost")).thenReturn(false);
        ApiException e = assertThrows(ApiException.class, () -> service.sendRequest("me", "ghost"));
        assertEquals("USER_NOT_FOUND", e.getCode());
    }

    @Test
    void 이미_친구면_409_FRIEND_ALREADY() {
        when(userRepository.existsByHandle("kim")).thenReturn(true);
        when(friendshipRepository.existsByOwnerHandleAndFriendHandle("me", "kim")).thenReturn(true);
        assertEquals("FRIEND_ALREADY",
                assertThrows(ApiException.class, () -> service.sendRequest("me", "kim")).getCode());
    }

    @Test
    void 상대가_먼저_신청했으면_자동수락_없이_409_FRIEND_REQUEST_INCOMING() {
        when(userRepository.existsByHandle("kim")).thenReturn(true);
        when(requestRepository.findByFromHandleAndToHandle("me", "kim")).thenReturn(Optional.empty());
        when(requestRepository.findByFromHandleAndToHandle("kim", "me"))
                .thenReturn(Optional.of(request(9L, "kim", "me")));

        ApiException e = assertThrows(ApiException.class, () -> service.sendRequest("me", "kim"));
        assertEquals("FRIEND_REQUEST_INCOMING", e.getCode());
        verify(requestRepository, never()).saveAndFlush(any());
        verify(friendshipRepository, never()).insertIfAbsent(any(), any(), any());
    }

    @Test
    void 동시_중복_신청은_유니크_위반을_409_FRIEND_REQUEST_EXISTS_로() {
        when(userRepository.existsByHandle("kim")).thenReturn(true);
        when(requestRepository.findByFromHandleAndToHandle(anyString(), anyString())).thenReturn(Optional.empty());
        when(requestRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_friend_request"));

        assertEquals("FRIEND_REQUEST_EXISTS",
                assertThrows(ApiException.class, () -> service.sendRequest("me", "kim")).getCode());
    }

    @Test
    void 신청_성공시_보낸_신청_형태로_상대_정보를_돌려준다() {
        when(userRepository.existsByHandle("kim")).thenReturn(true);
        when(requestRepository.findByFromHandleAndToHandle(anyString(), anyString())).thenReturn(Optional.empty());
        when(requestRepository.saveAndFlush(any())).thenReturn(request(7L, "me", "kim"));
        when(userRepository.findByHandle("kim")).thenReturn(Optional.of(user("kim")));

        FriendRequestDTO dto = service.sendRequest("me", " kim ");
        assertEquals(7L, dto.getId());
        assertEquals("kim", dto.getUser().getHandle());
        assertNull(dto.getUser().getTier());           // 시즌 미배치
        assertEquals(AT, dto.getUser().getLastActiveAt()); // 제출·랭킹 없으면 가입 시각
    }

    @Test
    void 수락하면_양방향_친구_행을_넣고_남은_신청을_정리한다() {
        when(requestRepository.findById(9L)).thenReturn(Optional.of(request(9L, "kim", "me")));
        when(requestRepository.deleteReceived(9L, "me")).thenReturn(1);

        service.acceptRequest("me", 9L);

        verify(friendshipRepository).insertIfAbsent(eq("me"), eq("kim"), any());
        verify(friendshipRepository).insertIfAbsent(eq("kim"), eq("me"), any());
        verify(requestRepository).deleteBetween("me", "kim");
    }

    @Test
    void 남의_신청이나_보낸_신청은_수락할_수_없다() {
        when(requestRepository.findById(9L)).thenReturn(Optional.of(request(9L, "me", "kim")));
        assertEquals("FRIEND_REQUEST_NOT_FOUND",
                assertThrows(ApiException.class, () -> service.acceptRequest("me", 9L)).getCode());
        verify(friendshipRepository, never()).insertIfAbsent(any(), any(), any());
    }

    @Test
    void 이미_처리된_신청을_동시에_수락하면_404() {
        when(requestRepository.findById(9L)).thenReturn(Optional.of(request(9L, "kim", "me")));
        when(requestRepository.deleteReceived(9L, "me")).thenReturn(0);
        assertEquals("FRIEND_REQUEST_NOT_FOUND",
                assertThrows(ApiException.class, () -> service.acceptRequest("me", 9L)).getCode());
        verify(friendshipRepository, never()).insertIfAbsent(any(), any(), any());
    }

    @Test
    void 친구가_아닌데_끊으면_404_FRIEND_NOT_FOUND() {
        when(friendshipRepository.deletePair("me", "kim")).thenReturn(0);
        assertEquals("FRIEND_NOT_FOUND",
                assertThrows(ApiException.class, () -> service.removeFriend("me", "kim")).getCode());
    }

    @Test
    void 관계없는_신청은_거절_취소할_수_없다() {
        when(requestRepository.deleteInvolving(5L, "me")).thenReturn(0);
        assertEquals("FRIEND_REQUEST_NOT_FOUND",
                assertThrows(ApiException.class, () -> service.deleteRequest("me", 5L)).getCode());
    }

    @Test
    void 빈_검색어는_DB를_조회하지_않고_빈_목록() {
        assertTrue(service.searchUsers("me", "  ").isEmpty());
        verifyNoInteractions(userRepository);
    }

    @Test
    void 검색_결과마다_나와의_관계와_신청_id를_붙인다() {
        when(userRepository.searchByHandle(eq("k"), eq("k"), any()))
                .thenReturn(List.of(user("me"), user("kim"), user("kang"), user("koo"), user("ko")));
        when(friendshipRepository.findFriendHandlesByOwnerHandle("me")).thenReturn(List.of("kim"));
        when(requestRepository.findByFromHandleAndToHandleIn(eq("me"), any()))
                .thenReturn(List.of(request(11L, "me", "kang")));
        when(requestRepository.findByToHandleAndFromHandleIn(eq("me"), any()))
                .thenReturn(List.of(request(12L, "koo", "me")));

        List<UserSearchItemDTO> result = service.searchUsers("me", "k");

        assertEquals(List.of(FriendRelation.SELF, FriendRelation.FRIENDS, FriendRelation.OUTGOING,
                        FriendRelation.INCOMING, FriendRelation.NONE),
                result.stream().map(UserSearchItemDTO::getRelation).toList());
        assertEquals(11L, result.get(2).getRequestId());
        assertEquals(12L, result.get(3).getRequestId());
        assertNull(result.get(1).getRequestId());
    }

    @Test
    void 검색어의_LIKE_와일드카드는_이스케이프된다() {
        assertEquals("a!_b!%c!!", FriendServiceImpl.escapeLike("a_b%c!"));
    }

    @Test
    void 프로필_관계_양쪽_신청이_있으면_받은_신청을_우선한다() {
        when(friendshipRepository.existsByOwnerHandleAndFriendHandle("me", "kim")).thenReturn(false);
        when(requestRepository.findByFromHandleAndToHandle("me", "kim")).thenReturn(Optional.of(request(1L, "me", "kim")));
        when(requestRepository.findByFromHandleAndToHandle("kim", "me")).thenReturn(Optional.of(request(2L, "kim", "me")));

        assertEquals(new FriendStatus(FriendRelation.INCOMING, 2L), service.statusOf("me", "kim"));
    }

    private static FriendRequestEntity request(Long id, String from, String to) {
        return new FriendRequestEntity(id, from, to, AT);
    }

    private static UserEntity user(String handle) {
        return UserEntity.createUserEntity(handle, handle + "@test.dev", "hash", AT);
    }
}
