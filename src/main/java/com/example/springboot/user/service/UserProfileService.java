package com.example.springboot.user.service;

import com.example.springboot.user.dto.UserProfileDTO;

public interface UserProfileService {

    /** GET /users/{handle} — 유저 프로필 (§2.15). 없는 handle 이면 null. */
    UserProfileDTO getProfile(String handle, String viewerHandle);

    /**
     * PUT /me/title — 대표 칭호 선택 (§2.16). 해제(null)는 항상 허용, 지정은 보유 칭호만.
     * 미보유 칭호(또는 없는 유저)면 false → 400.
     */
    boolean updateTitle(String email, String titleId);
}
