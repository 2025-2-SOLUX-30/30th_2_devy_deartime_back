package com.project.deartime.app.friend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.deartime.app.domain.Friend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponseDto {

    private Long userId;
    private Long friendId;
    private String friendNickname;
    private String friendProfileImageUrl;
    private String friendBio;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    public static FriendResponseDto from(Friend friend) {
        return FriendResponseDto.builder()
                .userId(friend.getUser().getId())
                .friendId(friend.getFriend().getId())
                .friendNickname(friend.getFriend().getNickname())
                .friendProfileImageUrl(friend.getFriend().getProfileImageUrl())
                .friendBio(friend.getFriend().getBio())
                .status(friend.getStatus())
                .requestedAt(friend.getRequestedAt())
                .build();
    }
}