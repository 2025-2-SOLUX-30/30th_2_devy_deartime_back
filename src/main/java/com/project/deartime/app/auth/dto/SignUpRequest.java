package com.project.deartime.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SignUpRequest {

    @NotBlank
    private String nickname;

    @NotNull
    private LocalDate birthDate;

    private String bio;

    @NotBlank
    private String profileImageUrl;
}