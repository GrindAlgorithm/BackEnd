package com.example.springboot.friend.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** GET /users/search 결과 1건 = FriendUser + 나와의 관계 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchItemDTO {
    @JsonUnwrapped
    private FriendUserDTO user;
    private FriendRelation relation;
    private Long requestId; // relation 이 incoming/outgoing 일 때만, 그 외 null
}
