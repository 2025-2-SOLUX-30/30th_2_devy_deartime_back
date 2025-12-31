package com.project.deartime.app.notification.dto;

import com.project.deartime.app.domain.Notification;
import com.project.deartime.app.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String senderNickname;
    private String contentTitle;
    private Long targetId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .senderNickname(notification.getSenderNickname())
                .contentTitle(notification.getContentTitle())
                .targetId(notification.getTargetId())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

