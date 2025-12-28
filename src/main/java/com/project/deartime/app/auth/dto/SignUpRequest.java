package com.project.deartime.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignUpRequest {

    @NotBlank
    private String nickname;

    private LocalDate birthDate;

    private String bio;

    // profileImageUrl은 선택 사항으로 변경
    private String profileImageUrl;
}