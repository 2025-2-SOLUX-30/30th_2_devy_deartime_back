package com.project.deartime.app.gallery.dto.photos;

import com.project.deartime.app.domain.Photo;
import com.project.deartime.app.domain.AlbumPhoto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PhotoDetailResponse(
        Long photoId,
        Long userId,
        String imageUrl,
        String caption,
        LocalDateTime uploadedAt,
        LocalDateTime takenAt,
        List<PhotoAlbumInfo> albums
) {

    public static PhotoDetailResponse fromEntity(Photo photo) {

        List<PhotoAlbumInfo> albumList = photo.getPhotoAlbums().stream()
                .map(AlbumPhoto::getAlbum)
                .map(PhotoAlbumInfo::fromAlbum)
                .collect(Collectors.toList());

        return new PhotoDetailResponse(
                photo.getId(),
                photo.getUser().getId(),
                photo.getImageUrl(),
                photo.getCaption() != null ? photo.getCaption() : "",
                photo.getCreatedAt(),
                photo.getTakenAt(),
                albumList
        );
    }

    public record PhotoAlbumInfo(
            Long albumId,
            String title
    ) {
        public static PhotoAlbumInfo fromAlbum(com.project.deartime.app.domain.Album album) {
            return new PhotoAlbumInfo(
                    album.getId(),
                    album.getTitle()
            );
        }
    }
}