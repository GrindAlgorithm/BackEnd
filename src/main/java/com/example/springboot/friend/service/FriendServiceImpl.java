package com.example.springboot.friend.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.friend.dto.FriendDTO;
import com.example.springboot.friend.dto.FriendRelation;
import com.example.springboot.friend.dto.FriendRequestDTO;
import com.example.springboot.friend.dto.FriendStatus;
import com.example.springboot.friend.dto.FriendUserDTO;
import com.example.springboot.friend.dto.FriendsResponseDTO;
import com.example.springboot.friend.dto.UserSearchItemDTO;
import com.example.springboot.friend.entity.FriendRequestEntity;
import com.example.springboot.friend.entity.FriendshipEntity;
import com.example.springboot.friend.repository.FriendRequestRepository;
import com.example.springboot.friend.repository.FriendshipRepository;
import com.example.springboot.problem.dto.TierRankDTO;
import com.example.springboot.ranking.entity.SeasonRankingEntity;
import com.example.springboot.ranking.repository.SeasonRankingRepository;
import com.example.springboot.season.entity.SeasonStatus;
import com.example.springboot.submission.entity.SubmissionStatus;
import com.example.springboot.submission.repository.SubmissionRepository;
import com.example.springboot.user.entity.UserEntity;
import com.example.springboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 친구 (신청 → 수락) — 연동 문서 §2.18.
 * 수락된 친구는 friendship 에 양방향 두 행으로 저장한다 — 랭킹 friends 스코프(§2.13)가 내 행만 읽어도 되도록.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FriendServiceImpl implements FriendService {

    static final int SEARCH_LIMIT = 10;

    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final SeasonRankingRepository rankingRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public FriendsResponseDTO getFriends(String me) {
        List<FriendshipEntity> friends = friendshipRepository.findByOwnerHandleOrderByCreatedAtDesc(me);
        List<FriendRequestEntity> incoming = requestRepository.findByToHandleOrderByCreatedAtDesc(me);
        List<FriendRequestEntity> outgoing = requestRepository.findByFromHandleOrderByCreatedAtDesc(me);

        Map<String, FriendUserDTO> users = friendUsers(Stream.of(
                friends.stream().map(FriendshipEntity::getFriendHandle),
                incoming.stream().map(FriendRequestEntity::getFromHandle),
                outgoing.stream().map(FriendRequestEntity::getToHandle)
        ).flatMap(Function.identity()).collect(Collectors.toCollection(LinkedHashSet::new)));

        return new FriendsResponseDTO(
                friends.stream()
                        .map(f -> new FriendDTO(users.get(f.getFriendHandle()), f.getCreatedAt()))
                        .toList(),
                incoming.stream()
                        .map(r -> new FriendRequestDTO(r.getId(), users.get(r.getFromHandle()), r.getCreatedAt()))
                        .toList(),
                outgoing.stream()
                        .map(r -> new FriendRequestDTO(r.getId(), users.get(r.getToHandle()), r.getCreatedAt()))
                        .toList()
        );
    }

    @Override
    public List<UserSearchItemDTO> searchUsers(String me, String query) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return List.of();
        }
        List<UserEntity> found = userRepository.searchByHandle(escapeLike(q), q, PageRequest.of(0, SEARCH_LIMIT));
        if (found.isEmpty()) {
            return List.of();
        }
        List<String> handles = found.stream().map(UserEntity::getHandle).toList();

        Set<String> friends = new HashSet<>(friendshipRepository.findFriendHandlesByOwnerHandle(me));
        Map<String, Long> outgoing = requestRepository.findByFromHandleAndToHandleIn(me, handles).stream()
                .collect(Collectors.toMap(FriendRequestEntity::getToHandle, FriendRequestEntity::getId));
        Map<String, Long> incoming = requestRepository.findByToHandleAndFromHandleIn(me, handles).stream()
                .collect(Collectors.toMap(FriendRequestEntity::getFromHandle, FriendRequestEntity::getId));
        Map<String, FriendUserDTO> users = friendUsers(handles);

        return handles.stream().map(h -> {
            FriendStatus status = relation(me, h, friends.contains(h), outgoing.get(h), incoming.get(h));
            return new UserSearchItemDTO(users.get(h), status.relation(), status.requestId());
        }).toList();
    }

    @Override
    @Transactional
    public FriendRequestDTO sendRequest(String me, String targetHandle) {
        String target = targetHandle == null ? "" : targetHandle.trim();
        if (target.equals(me)) {
            throw ApiException.badRequest("FRIEND_SELF", "자기 자신에게는 신청할 수 없습니다");
        }
        if (!userRepository.existsByHandle(target)) {
            throw ApiException.notFound("USER_NOT_FOUND", "존재하지 않는 핸들입니다: " + target);
        }
        if (friendshipRepository.existsByOwnerHandleAndFriendHandle(me, target)) {
            throw ApiException.conflict("FRIEND_ALREADY", "이미 " + target + " 님과 친구입니다");
        }
        if (requestRepository.findByFromHandleAndToHandle(me, target).isPresent()) {
            throw ApiException.conflict("FRIEND_REQUEST_EXISTS", "이미 신청을 보냈습니다");
        }
        // 자동 수락하지 않는다 — 프론트가 받은 신청의 수락 버튼으로 안내한다 (§2.18)
        if (requestRepository.findByFromHandleAndToHandle(target, me).isPresent()) {
            throw ApiException.conflict("FRIEND_REQUEST_INCOMING",
                    target + " 님이 먼저 신청했습니다. 받은 신청에서 수락하세요");
        }

        FriendRequestEntity saved;
        try {
            saved = requestRepository.saveAndFlush(
                    FriendRequestEntity.createFriendRequestEntity(me, target, now()));
        } catch (DataIntegrityViolationException e) {
            // 같은 신청을 동시에 두 번 보낸 경우 — uq_friend_request 가 막는다
            throw ApiException.conflict("FRIEND_REQUEST_EXISTS", "이미 신청을 보냈습니다");
        }
        log.info("friend request sent : from={}, to={}, id={}", me, target, saved.getId());
        return new FriendRequestDTO(saved.getId(), friendUsers(List.of(target)).get(target), saved.getCreatedAt());
    }

    @Override
    @Transactional
    public void acceptRequest(String me, Long requestId) {
        FriendRequestEntity request = requestRepository.findById(requestId)
                .filter(r -> r.getToHandle().equals(me))
                .orElseThrow(FriendServiceImpl::requestNotFound);
        // 조건부 DELETE 가 0이면 그사이 다른 요청이 먼저 처리(수락·취소)한 것
        if (requestRepository.deleteReceived(requestId, me) == 0) {
            throw requestNotFound();
        }
        String other = request.getFromHandle();
        LocalDateTime now = now();
        friendshipRepository.insertIfAbsent(me, other, now);
        friendshipRepository.insertIfAbsent(other, me, now);
        // 서로 동시에 신청해 역방향 신청이 남아 있을 수 있다
        requestRepository.deleteBetween(me, other);
        log.info("friend request accepted : id={}, {} <-> {}", requestId, other, me);
    }

    @Override
    @Transactional
    public void deleteRequest(String me, Long requestId) {
        if (requestRepository.deleteInvolving(requestId, me) == 0) {
            throw requestNotFound();
        }
    }

    @Override
    @Transactional
    public void removeFriend(String me, String friendHandle) {
        if (friendshipRepository.deletePair(me, friendHandle) == 0) {
            throw ApiException.notFound("FRIEND_NOT_FOUND", friendHandle + " 님은 친구가 아닙니다");
        }
        log.info("friend removed : {} x {}", me, friendHandle);
    }

    @Override
    public FriendStatus statusOf(String me, String other) {
        return relation(me, other,
                friendshipRepository.existsByOwnerHandleAndFriendHandle(me, other),
                requestRepository.findByFromHandleAndToHandle(me, other).map(FriendRequestEntity::getId).orElse(null),
                requestRepository.findByFromHandleAndToHandle(other, me).map(FriendRequestEntity::getId).orElse(null));
    }

    private static FriendStatus relation(String me, String other, boolean friends, Long outgoingId, Long incomingId) {
        if (other.equals(me)) {
            return new FriendStatus(FriendRelation.SELF, null);
        }
        if (friends) {
            return new FriendStatus(FriendRelation.FRIENDS, null);
        }
        // 양쪽 신청이 동시에 남아 있으면 상대 신청을 우선 — 수락 한 번으로 둘 다 정리된다
        if (incomingId != null) {
            return new FriendStatus(FriendRelation.INCOMING, incomingId);
        }
        if (outgoingId != null) {
            return new FriendStatus(FriendRelation.OUTGOING, outgoingId);
        }
        return new FriendStatus(FriendRelation.NONE, null);
    }

    /**
     * handle → FriendUser. 티어는 현재 시즌 랭킹, solvedCount·lastActiveAt 은 제출 이력 기준.
     * 제출이 없으면 lastActiveAt 은 시즌 랭킹 활동 시각 → 가입 시각 순으로 채운다(데모 시드 유저 대비).
     */
    private Map<String, FriendUserDTO> friendUsers(Collection<String> handles) {
        if (handles.isEmpty()) {
            return Map.of();
        }
        Map<String, SeasonRankingEntity> rankings = rankingRepository
                .findBySeason_StatusAndHandleIn(SeasonStatus.CURRENT, handles).stream()
                .collect(Collectors.toMap(SeasonRankingEntity::getHandle, Function.identity(), (a, b) -> a));
        Map<String, Object[]> summaries = submissionRepository
                .summarizeByHandles(handles, SubmissionStatus.ACCEPTED).stream()
                .collect(Collectors.toMap(row -> (String) row[0], Function.identity()));

        Map<String, FriendUserDTO> users = new HashMap<>();
        for (String h : handles) {
            SeasonRankingEntity ranking = rankings.get(h);
            Object[] summary = summaries.get(h);
            TierRankDTO tier = ranking == null ? null : TierRankDTO.of(ranking.getTierName(), ranking.getTierLevel());
            int solved = summary == null ? 0 : ((Number) summary[1]).intValue();
            LocalDateTime lastActive = summary != null ? (LocalDateTime) summary[2]
                    : ranking != null ? ranking.getLastActiveAt()
                    : userRepository.findByHandle(h).map(UserEntity::getJoinedAt).orElse(null);
            users.put(h, new FriendUserDTO(h, tier, solved, lastActive));
        }
        return users;
    }

    /** DB(DATETIME)가 초 단위라 응답 시각도 초에서 자른다 — 저장 직후 응답과 재조회 값이 같도록 */
    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /** LIKE 와일드카드 이스케이프 — 이스케이프 문자는 '!' (UserRepository.searchByHandle) */
    static String escapeLike(String q) {
        return q.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private static ApiException requestNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "FRIEND_REQUEST_NOT_FOUND", "신청이 없거나 이미 처리되었습니다");
    }
}
