package com.project.deartime.app.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendSearchResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private String friendStatus; // none, pending, received, accepted, blocked
}