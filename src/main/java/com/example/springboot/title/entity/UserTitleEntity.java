package com.example.springboot.title.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 보유 칭호 (user-title-schema.sql). titleId = season_reward.id */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_title")
public class UserTitleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String handle;

    @Column(name = "title_id", nullable = false, length = 32)
    private String titleId;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;
}
