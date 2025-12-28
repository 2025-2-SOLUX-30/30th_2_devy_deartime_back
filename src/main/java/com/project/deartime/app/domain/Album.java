package com.project.deartime.app.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "albums")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계: user_id (앨범 소유자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_photo_id")
    private Photo coverPhoto;

    // N:M 관계 (AlbumPhotos 중간 테이블을 통해 Photo 목록 관리)
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL)
    private Set<AlbumPhoto> albumPhotos = new HashSet<>();

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateCoverPhoto(Photo coverPhoto) {
        this.coverPhoto = coverPhoto;
    }
}
