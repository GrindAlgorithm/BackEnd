package com.example.springboot.friend.dto;

import com.example.springboot.problem.dto.TierRankDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 연동 문서 §2.18 FriendUser — 친구 화면·검색 결과의 공통 유저 요약 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendUserDTO {
    private String handle;
    private TierRankDTO tier;          // 현재 시즌 미배치면 null
    private int solvedCount;           // 전체 기간 해결한 문제 수 (프로필 stats.solvedCount 와 같은 기준)
    private LocalDateTime lastActiveAt; // 마지막 제출 시각. 제출이 없으면 시즌 랭킹 활동 시각 → 가입 시각
}
