package com.project.deartime.app.friend.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendSearchResponse {

    private Long userId;

    private String nickname;

    private String profileImageUrl;

    private String bio;

    /**
     * 친구 상태
     * - "none": 친구 관계 없음
     * - "pending": 내가 친구 요청을 보낸 상태
     * - "received": 상대방이 나에게 친구 요청을 보낸 상태
     * - "accepted": 친구 상태
     * - "blocked": 차단 상태
     */
    private String friendStatus;
}