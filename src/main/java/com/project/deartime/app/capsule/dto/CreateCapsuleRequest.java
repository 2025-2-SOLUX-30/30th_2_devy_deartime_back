package com.project.deartime.app.capsule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCapsuleRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;


    private String theme;

    @NotNull(message = "받는 사람 ID는 필수입니다.")
    private Long receiverId;

    @NotNull(message = "개봉 일시는 필수입니다.")
    private LocalDateTime openAt;
}

