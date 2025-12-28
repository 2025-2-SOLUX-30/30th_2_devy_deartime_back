package com.project.deartime.app.gallery.dto.photos;

import com.project.deartime.app.domain.Photo;

import java.time.LocalDateTime;

public record PhotoListResponse (
        Long photoId,
        Long userId,
        String imageUrl,
        String caption,
        LocalDateTime takenAt
){
    public static PhotoListResponse fromEntity(Photo photo) {
        return new PhotoListResponse(
                photo.getId(),
                photo.getUser().getId(),
                photo.getImageUrl(),
                photo.getCaption() != null ? photo.getCaption() : "",
                photo.getTakenAt()
        );
    }
}
