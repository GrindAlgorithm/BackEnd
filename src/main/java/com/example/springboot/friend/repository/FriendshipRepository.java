package com.example.springboot.friend.repository;

import com.example.springboot.friend.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {

    /** 내(ownerHandle)가 친구로 추가한 handle 목록 — 랭킹 friends 스코프에서 필터에 사용 */
    @Query("SELECT f.friendHandle FROM FriendshipEntity f WHERE f.ownerHandle = :ownerHandle")
    List<String> findFriendHandlesByOwnerHandle(@Param("ownerHandle") String ownerHandle);

    /** 친구 화면 목록 — 최근에 친구가 된 순 (§2.18) */
    List<FriendshipEntity> findByOwnerHandleOrderByCreatedAtDesc(String ownerHandle);

    boolean existsByOwnerHandleAndFriendHandle(String ownerHandle, String friendHandle);

    /**
     * 친구 행이 없을 때만 넣는다 — 수락이 겹쳐도 uq_friendship 으로 터지지 않게 DB 가 판정한다.
     * (SeasonRankingRepository.insertIfAbsent 와 같은 이유로 IGNORE 대신 ODKU)
     */
    @Modifying
    @Query(value = "INSERT INTO friendship (owner_handle, friend_handle, created_at) "
            + "VALUES (:owner, :friend, :at) "
            + "ON DUPLICATE KEY UPDATE owner_handle = owner_handle", nativeQuery = true)
    int insertIfAbsent(@Param("owner") String ownerHandle, @Param("friend") String friendHandle,
                       @Param("at") LocalDateTime at);

    /** 친구 끊기 — 양방향 행을 한 번에 지운다. 지운 행 수를 반환 */
    @Modifying
    @Query("DELETE FROM FriendshipEntity f WHERE (f.ownerHandle = :a AND f.friendHandle = :b) "
            + "OR (f.ownerHandle = :b AND f.friendHandle = :a)")
    int deletePair(@Param("a") String a, @Param("b") String b);
}
