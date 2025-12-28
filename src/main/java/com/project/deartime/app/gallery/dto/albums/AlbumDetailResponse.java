package com.project.deartime.app.gallery.dto.albums;

import com.project.deartime.app.domain.Album;
import java.time.LocalDateTime;

public record AlbumDetailResponse(
        Long albumId,
        Long userId,
        String title,
        String coverImageUrl,
        String ownerNickname,
        LocalDateTime createdAt
) {
    public static AlbumDetailResponse fromEntity(Album album) {
        return new AlbumDetailResponse(
                album.getId(),
                album.getUser().getId(),
                album.getTitle(),
                album.getCoverPhoto() != null
                        ? album.getCoverPhoto().getImageUrl()
                        : null,
                album.getUser().getNickname(),
                album.getCreatedAt()
        );
    }
}
