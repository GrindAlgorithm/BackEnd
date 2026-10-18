package com.example.springboot.friend.repository;

import com.example.springboot.friend.entity.FriendRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Long> {

    /** 받은 신청 — 최신순 */
    List<FriendRequestEntity> findByToHandleOrderByCreatedAtDesc(String toHandle);

    /** 보낸 신청 — 최신순 */
    List<FriendRequestEntity> findByFromHandleOrderByCreatedAtDesc(String fromHandle);

    Optional<FriendRequestEntity> findByFromHandleAndToHandle(String fromHandle, String toHandle);

    /** 검색 결과 관계 판정용 — 내가 보낸 신청 중 상대가 handles 에 있는 것 */
    List<FriendRequestEntity> findByFromHandleAndToHandleIn(String fromHandle, Collection<String> toHandles);

    /** 검색 결과 관계 판정용 — 내가 받은 신청 중 보낸 사람이 handles 에 있는 것 */
    List<FriendRequestEntity> findByToHandleAndFromHandleIn(String toHandle, Collection<String> fromHandles);

    /**
     * 수락 — 내가 받은 신청만 지운다. 지운 행 수가 0이면 없거나 남의 신청(또는 이미 처리됨).
     * 조회 후 삭제가 아니라 조건부 DELETE 라서 같은 신청을 동시에 두 번 수락해도 한쪽만 1을 받는다.
     */
    @Modifying
    @Query("DELETE FROM FriendRequestEntity r WHERE r.id = :id AND r.toHandle = :me")
    int deleteReceived(@Param("id") Long id, @Param("me") String me);

    /** 거절·취소 공용 — 내가 보낸 사람이거나 받은 사람인 신청만 지운다 */
    @Modifying
    @Query("DELETE FROM FriendRequestEntity r WHERE r.id = :id AND (r.fromHandle = :me OR r.toHandle = :me)")
    int deleteInvolving(@Param("id") Long id, @Param("me") String me);

    /** 두 사람 사이의 신청(양방향)을 모두 지운다 — 수락 후 남은 역방향 신청 정리용 */
    @Modifying
    @Query("DELETE FROM FriendRequestEntity r WHERE (r.fromHandle = :a AND r.toHandle = :b) "
            + "OR (r.fromHandle = :b AND r.toHandle = :a)")
    int deleteBetween(@Param("a") String a, @Param("b") String b);
}
