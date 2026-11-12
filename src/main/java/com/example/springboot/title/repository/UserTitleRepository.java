package com.example.springboot.title.repository;

import com.example.springboot.title.entity.UserTitleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserTitleRepository extends JpaRepository<UserTitleEntity, Long> {

    List<UserTitleEntity> findByHandle(String handle);

    boolean existsByHandleAndTitleId(String handle, String titleId);

    /**
     * 없을 때만 발급 — 채점 직후 발급과 자정 일괄 발급이 겹쳐도 중복키로 터지지 않게 DB 가 판정한다.
     *
     * @return 새로 발급했으면 1, 이미 있었으면 0
     */
    @Modifying
    @Query(value = "INSERT IGNORE INTO user_title (handle, title_id, acquired_at) VALUES (:handle, :titleId, :at)",
            nativeQuery = true)
    int insertIfAbsent(@Param("handle") String handle, @Param("titleId") String titleId,
                       @Param("at") LocalDateTime at);
}
