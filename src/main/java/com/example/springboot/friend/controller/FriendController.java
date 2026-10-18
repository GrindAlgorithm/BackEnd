package com.example.springboot.friend.controller;

import com.example.springboot.friend.dto.FriendRequestCreateDTO;
import com.example.springboot.friend.dto.FriendRequestDTO;
import com.example.springboot.friend.dto.FriendsResponseDTO;
import com.example.springboot.friend.dto.UserSearchItemDTO;
import com.example.springboot.friend.service.FriendService;
import com.example.springboot.user.CurrentUserProvider;
import com.example.springboot.util.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 친구 (신청 → 수락) — 연동 문서 §2.18. 전부 로그인 필요(비로그인 401).
 * /users/search 는 /users/** permitAll 아래라서 시큐리티가 아니라 requireHandle 이 401 을 낸다.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/friends")
    public ResponseResult<FriendsResponseDTO> getFriends() {
        return ResponseResult.success(friendService.getFriends(currentUserProvider.requireHandle()));
    }

    @GetMapping("/users/search")
    public ResponseResult<List<UserSearchItemDTO>> searchUsers(@RequestParam(defaultValue = "") String q) {
        return ResponseResult.success(friendService.searchUsers(currentUserProvider.requireHandle(), q));
    }

    @PostMapping("/friends/requests")
    public ResponseEntity<ResponseResult<FriendRequestDTO>> sendRequest(@Valid @RequestBody FriendRequestCreateDTO request) {
        FriendRequestDTO created = friendService.sendRequest(currentUserProvider.requireHandle(), request.getHandle());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseResult.success(created));
    }

    @PostMapping("/friends/requests/{id}/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long id) {
        friendService.acceptRequest(currentUserProvider.requireHandle(), id);
        return ResponseEntity.noContent().build();
    }

    /** 받은 신청 거절 · 보낸 신청 취소 공용 */
    @DeleteMapping("/friends/requests/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        friendService.deleteRequest(currentUserProvider.requireHandle(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/friends/{handle}")
    public ResponseEntity<Void> removeFriend(@PathVariable String handle) {
        friendService.removeFriend(currentUserProvider.requireHandle(), handle);
        return ResponseEntity.noContent().build();
    }
}
