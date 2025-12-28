package com.project.deartime.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Size(max = 500, message = "자기소개는 500자 이하여야 합니다.")
    private String bio;

    private String profileImageUrl;
}