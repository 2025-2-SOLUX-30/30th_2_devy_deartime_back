package com.project.deartime.app.letter.dto;

import java.time.LocalDateTime;

public record LetterSendResponse(
        Long letterId,
        String senderNickname,
        String receiverNickname,
        LocalDateTime sentAt,
        String message,
        String warningMessage
) {
}
