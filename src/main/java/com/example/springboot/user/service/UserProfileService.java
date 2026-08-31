package com.example.springboot.user.service;

import com.example.springboot.user.dto.UserProfileDTO;

public interface UserProfileService {

    /** GET /users/{handle} — 유저 프로필 (§2.15). 없는 handle 이면 null. */
    UserProfileDTO getProfile(String handle, String viewerHandle);

    /**
     * PUT /me/title — 대표 칭호 선택 (§2.16). 해제(null)만 허용.
     * 칭호 발급 도메인(A4) 구현 전이라 보유 칭호가 없으므로, 비-null 지정은 미보유(false)로 거부한다.
     */
    boolean updateTitle(String email, String titleId);
}
