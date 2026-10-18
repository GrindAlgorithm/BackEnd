package com.example.springboot.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** GET /friends — 친구 화면 통합 응답 (§2.18) */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendsResponseDTO {
    private List<FriendDTO> friends;
    private List<FriendRequestDTO> incoming;
    private List<FriendRequestDTO> outgoing;
}
