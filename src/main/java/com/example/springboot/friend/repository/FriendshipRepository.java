package com.example.springboot.friend.repository;

import com.example.springboot.friend.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {

    /** 내(ownerHandle)가 친구로 추가한 handle 목록 — 랭킹 friends 스코프에서 필터에 사용 */
    @Query("SELECT f.friendHandle FROM FriendshipEntity f WHERE f.ownerHandle = :ownerHandle")
    List<String> findFriendHandlesByOwnerHandle(@Param("ownerHandle") String ownerHandle);
}
