package com.example.springboot.friend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST /friends/requests 요청 */
@Getter
@Setter
@NoArgsConstructor
public class FriendRequestCreateDTO {
    @NotBlank(message = "신청할 핸들을 입력하세요")
    private String handle;
}
