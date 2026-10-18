package com.example.springboot.friend.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 친구 1명 = FriendUser + since(친구가 된 시각) */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {
    @JsonUnwrapped
    private FriendUserDTO user;
    private LocalDateTime since;
}
