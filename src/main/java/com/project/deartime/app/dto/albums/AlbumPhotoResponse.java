package com.project.deartime.app.dto.albums;

import com.project.deartime.app.domain.AlbumPhoto;

public record AlbumPhotoResponse(
        Long photoId,
        Long albumId
) {
    public static AlbumPhotoResponse fromEntity(AlbumPhoto albumPhoto) {
        return new AlbumPhotoResponse(
                albumPhoto.getPhoto().getId(),
                albumPhoto.getAlbum().getId()
        );
    }
}
