package com.project.deartime.app.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {

    @NotNull(message = "친구 ID를 입력해주세요.")
    private Long friendId;
}