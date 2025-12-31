package com.project.deartime.app.capsule.dto;
import com.project.deartime.app.domain.TimeCapsule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapsuleResponse {

    private Long id;

    private String title;

    private String content;

    private String theme;

    private String imageUrl;

    private LocalDateTime openAt;

    private Boolean isNotified;
    
    private Long senderId;

    private String senderNickname;

    private String senderProfileImageUrl;

    private Long receiverId;

    private String receiverNickname;

    private String receiverProfileImageUrl;

    private LocalDateTime createdAt;

    private boolean isOpened;

    private boolean canAccess;

    public static CapsuleResponse from(TimeCapsule capsule, boolean canAccess) {
        // openAt 시간 이후(openAt 포함)인 경우 opened 상태
        boolean isOpened = !capsule.getOpenAt().isAfter(LocalDateTime.now());

        return CapsuleResponse.builder()
                .id(capsule.getId())
                .title(capsule.getTitle())
                .content(canAccess ? capsule.getContent() : null)
                .theme(capsule.getTheme())
                .imageUrl(capsule.getImageUrl())
                .openAt(capsule.getOpenAt())
                .isNotified(capsule.getIsNotified())
                .senderId(capsule.getSender().getId())
                .senderNickname(capsule.getSender().getNickname())
                .senderProfileImageUrl(capsule.getSender().getProfileImageUrl())
                .receiverId(capsule.getReceiver().getId())
                .receiverNickname(capsule.getReceiver().getNickname())
                .receiverProfileImageUrl(capsule.getReceiver().getProfileImageUrl())
                .createdAt(capsule.getCreatedAt())
                .isOpened(isOpened)
                .canAccess(canAccess)
                .build();
    }
}

