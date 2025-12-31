package com.project.deartime.app.notification.domain;

/**
 * 알림 유형
 */
public enum NotificationType {
    LETTER_RECEIVED("편지를 보냈습니다."),
    CAPSULE_RECEIVED("타임캡슐을 보냈습니다."),
    CAPSULE_OPENED("타임캡슐을 열어볼 수 있습니다."),
    FRIEND_REQUEST("친구 요청을 보냈습니다."),
    FRIEND_ACCEPT("친구 요청을 수락했습니다.");

    private final String defaultMessage;

    NotificationType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

