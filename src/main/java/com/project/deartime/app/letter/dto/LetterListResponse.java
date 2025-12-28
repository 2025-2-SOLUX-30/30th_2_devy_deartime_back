package com.project.deartime.app.letter.dto;

import com.project.deartime.app.domain.Letter;

import java.time.LocalDateTime;

public record LetterListResponse(
        Long letterId,
        String senderNickname,
        String receiverNickname,
        String title,
        String summary,
        String themeCode,
        LocalDateTime sentAt,
        boolean isRead,
        boolean isBookmarked
) {
    public static LetterListResponse fromEntity(Letter letter, boolean isBookmarked) {
        String themeCode = letter.getTheme() != null ? letter.getTheme().getCode() : null;
        String fullContent = letter.getContent();
        String summary = (fullContent.length() >= 50)
                ? fullContent.substring(0, 50) + "..."
                : fullContent;

        return new LetterListResponse(
                letter.getId(),
                letter.getSender().getNickname(),
                letter.getReceiver().getNickname(),
                letter.getTitle(),
                summary,
                themeCode,
                letter.getCreatedAt(),
                letter.getIsRead(),
                isBookmarked
        );
    }
}
