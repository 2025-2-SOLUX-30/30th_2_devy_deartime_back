package com.project.deartime.app.dto;

import com.project.deartime.app.domain.Letter;

import java.time.LocalDateTime;

public record LetterListResponse(
        Long letterId,
        String senderNickname,
        String receiverNickname,
        String title,
        String themeCode,
        LocalDateTime sentAt,
        boolean isRead,
        boolean isBookmarked
) {
    public static LetterListResponse fromEntity(Letter letter, boolean isBookmarked) {
        String themeCode = letter.getTheme() != null ? letter.getTheme().getCode() : null;
        return new LetterListResponse(
                letter.getId(),
                letter.getSender().getNickname(),
                letter.getReceiver().getNickname(),
                letter.getTitle(),
                themeCode,
                letter.getCreatedAt(),
                letter.getIsRead(),
                isBookmarked
        );
    }
}
