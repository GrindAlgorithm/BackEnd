package com.example.springboot.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 친구 신청 — user 는 받은 신청이면 보낸 사람, 보낸 신청이면 받는 사람 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDTO {
    private Long id;
    private FriendUserDTO user;
    private LocalDateTime createdAt;
}
