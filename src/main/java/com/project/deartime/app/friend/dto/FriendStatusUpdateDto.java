package com.project.deartime.app.friend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendStatusUpdateDto {

    @NotBlank(message = "상태값을 입력해주세요.")
    @Pattern(regexp = "^(accepted|rejected|blocked)$",
            message = "상태값은 accepted, rejected, blocked 중 하나여야 합니다.")
    private String status;
}