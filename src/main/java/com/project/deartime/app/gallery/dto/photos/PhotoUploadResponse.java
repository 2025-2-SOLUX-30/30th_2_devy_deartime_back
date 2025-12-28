package com.project.deartime.app.gallery.dto.photos;

import java.time.LocalDateTime;

public record PhotoUploadResponse(
        Long photoId,
        String imageUrl,
        String caption,
        LocalDateTime uploadedAt,
        String message
) {
}
