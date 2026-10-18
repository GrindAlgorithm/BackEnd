package com.example.springboot.friend.service;

import com.example.springboot.friend.dto.FriendRequestDTO;
import com.example.springboot.friend.dto.FriendStatus;
import com.example.springboot.friend.dto.FriendsResponseDTO;
import com.example.springboot.friend.dto.UserSearchItemDTO;

import java.util.List;

/** 친구 (신청 → 수락) — 연동 문서 §2.18. me 는 전부 로그인 유저 handle */
public interface FriendService {

    /** GET /friends — 친구·받은 신청·보낸 신청 */
    FriendsResponseDTO getFriends(String me);

    /** GET /users/search — 핸들 부분 일치, 최대 10건. 빈 q 는 빈 목록 */
    List<UserSearchItemDTO> searchUsers(String me, String query);

    /** POST /friends/requests — 신청. 보낸 신청(outgoing 형태)을 반환 */
    FriendRequestDTO sendRequest(String me, String targetHandle);

    /** POST /friends/requests/{id}/accept — 받은 신청 수락 */
    void acceptRequest(String me, Long requestId);

    /** DELETE /friends/requests/{id} — 받은 신청 거절 · 보낸 신청 취소 */
    void deleteRequest(String me, Long requestId);

    /** DELETE /friends/{handle} — 친구 끊기(양쪽) */
    void removeFriend(String me, String friendHandle);

    /** 프로필(§2.15) friendRelation/friendRequestId 용 — me 기준 other 와의 관계 */
    FriendStatus statusOf(String me, String other);
}
