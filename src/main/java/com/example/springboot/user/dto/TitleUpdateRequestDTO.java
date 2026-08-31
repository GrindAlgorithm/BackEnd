package com.example.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PUT /me/title 요청 (§2.16) — { "titleId": "s1_clear" } 또는 해제 { "titleId": null } */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TitleUpdateRequestDTO {
    private String titleId;
}
