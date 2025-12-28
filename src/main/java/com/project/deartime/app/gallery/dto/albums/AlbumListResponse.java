package com.project.deartime.app.gallery.dto.albums;

import com.project.deartime.app.domain.Album;
import java.time.LocalDateTime;

public record AlbumListResponse(
        Long albumId,
        Long userId,
        String title,
        String coverImageUrl,
        LocalDateTime createdAt
) {
    public static AlbumListResponse fromEntity(Album album) {
        return new AlbumListResponse(
                album.getId(),
                album.getUser().getId(),
                album.getTitle(),
                album.getCoverPhoto() != null
                        ? album.getCoverPhoto().getImageUrl()
                        : null,
                album.getCreatedAt()
        );
    }
}
