package com.project.deartime.app.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "album_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(AlbumPhotoId.class)
@Builder
@AllArgsConstructor
public class AlbumPhoto extends BaseTimeEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;
}
