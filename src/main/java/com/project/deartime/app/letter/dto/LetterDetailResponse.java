package com.project.deartime.app.letter.dto;

import com.project.deartime.app.domain.Letter;

import java.time.LocalDateTime;

public record LetterDetailResponse(
        Long letterId,
        Long senderId,
        String senderNickname,
        Long receiverId,
        String receiverNickname,
        String title,
        String content,
        String themeCode,
        LocalDateTime sentAt,
        boolean isRead,
        boolean isBookmarked
) {
    public static LetterDetailResponse fromEntity(Letter letter, boolean isBookmarked) {
        String themeCode = letter.getTheme() != null ? letter.getTheme().getCode() : null;

        return new LetterDetailResponse(
                letter.getId(),
                letter.getSender().getId(),
                letter.getSender().getNickname(),
                letter.getReceiver().getId(),
                letter.getReceiver().getNickname(),
                letter.getTitle(),
                letter.getContent(),
                themeCode,
                letter.getCreatedAt(),
                letter.getIsRead(),
                isBookmarked
        );
    }
}
