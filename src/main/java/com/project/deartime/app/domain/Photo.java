package com.project.deartime.app.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "photos")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계: user_id (사진 업로드 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    // N:M 관계 (AlbumPhotos 중간 테이블을 통해 앨범 목록 관리)
    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL)
    private Set<AlbumPhoto> photoAlbums = new HashSet<>();

    public void updateCaption(String caption) {
        this.caption = caption;
    }
}
