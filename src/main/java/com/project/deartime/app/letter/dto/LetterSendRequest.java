package com.project.deartime.app.letter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record LetterSendRequest(
        @NotNull(message = "수신자 ID는 필수입니다.")
        Long receiverId,

        String theme,

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        String content
) {
}
