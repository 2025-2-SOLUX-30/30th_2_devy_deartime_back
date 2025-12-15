package com.project.deartime.app.dto.albums;

import com.project.deartime.app.domain.Album;
import java.time.LocalDateTime;

public record AlbumDetailResponse(
        Long albumId,
        Long userId,
        String title,
        String coverUrl,
        String ownerNickname,
        LocalDateTime createdAt
) {
    public static AlbumDetailResponse fromEntity(Album album) {
        return new AlbumDetailResponse(
                album.getId(),
                album.getUser().getId(),
                album.getTitle(),
                album.getCoverUrl(),
                album.getUser().getNickname(),
                album.getCreatedAt()
        );
    }
}
