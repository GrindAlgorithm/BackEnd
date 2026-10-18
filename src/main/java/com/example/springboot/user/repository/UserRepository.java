package com.example.springboot.user.repository;

import com.example.springboot.user.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByHandle(String handle);

    boolean existsByEmail(String email);

    boolean existsByHandle(String handle);

    /**
     * 핸들 부분 일치 검색(대소문자 무시) — 정확히 일치 → 짧은 핸들 → 사전순 (§2.18 GET /users/search).
     * pattern 은 호출 측에서 %·_ 를 '!' 로 이스케이프한 값이다(백슬래시는 MariaDB 문자열 이스케이프와 겹쳐 피함).
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.handle) LIKE CONCAT('%', LOWER(:pattern), '%') ESCAPE '!' "
            + "ORDER BY CASE WHEN LOWER(u.handle) = LOWER(:exact) THEN 0 ELSE 1 END, LENGTH(u.handle), u.handle")
    List<UserEntity> searchByHandle(@Param("pattern") String pattern, @Param("exact") String exact, Pageable pageable);
}
