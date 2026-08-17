package com.example.springboot.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 유저(회원) — 로그인/회원가입.
 * 테이블명은 app_user (MariaDB 시스템 테이블 user 회피). handle 은 기존 도메인
 * (submission.user_handle, season_ranking.handle)이 참조하던 유저 식별자다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String handle;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "selected_title_id", length = 32)
    private String selectedTitleId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /** 신규 가입 — 칭호는 미선택(null). */
    public static UserEntity createUserEntity(String handle, String email, String passwordHash, LocalDateTime joinedAt) {
        return new UserEntity(null, handle, email, passwordHash, null, joinedAt);
    }

    /** 대표 칭호 변경 (PUT /me/title) */
    public void changeTitle(String titleId) {
        this.selectedTitleId = titleId;
    }
}
