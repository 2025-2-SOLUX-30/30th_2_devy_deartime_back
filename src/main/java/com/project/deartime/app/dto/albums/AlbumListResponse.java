package com.project.deartime.app.dto.albums;

import com.project.deartime.app.domain.Album;
import java.time.LocalDateTime;

public record AlbumListResponse(
        Long albumId,
        Long userId,
        String title,
        String coverUrl,
        LocalDateTime createdAt
) {
    public static AlbumListResponse fromEntity(Album album) {
        return new AlbumListResponse(
                album.getId(),
                album.getUser().getId(),
                album.getTitle(),
                album.getCoverUrl(),
                album.getCreatedAt()
        );
    }
}
