package com.seein.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 닉네임 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class NicknameUpdateRequest {

    @NotBlank(message = "닉네임은 필수 입력입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내로 입력해주세요.")
    private String nickname;
}
