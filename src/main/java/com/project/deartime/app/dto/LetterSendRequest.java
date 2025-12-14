package com.project.deartime.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record LetterSendRequest(
        Long senderId,

        @NotBlank(message = "수신자 닉네임은 필수입니다.")
        String receiverNickname,

        String theme,

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        String content,

        LocalDateTime sentAt
) {
}
