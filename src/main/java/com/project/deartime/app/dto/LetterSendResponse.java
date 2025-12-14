package com.project.deartime.app.dto;

import java.time.LocalDateTime;

public record LetterSendResponse(
        Long letterId,
        String senderNickname,
        String receiverNickname,
        LocalDateTime sentAt,
        String message
) {
}
