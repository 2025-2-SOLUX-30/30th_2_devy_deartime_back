package com.project.deartime.app.gallery.repository;

import com.project.deartime.app.domain.AlbumPhoto;
import com.project.deartime.app.domain.AlbumPhotoId;
import com.project.deartime.app.domain.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, AlbumPhotoId> {
    // 특정 앨범에 속한 모든 사진 조회
    @Query("SELECT ap.photo FROM AlbumPhoto ap WHERE ap.album.id = :albumId")
    Page<Photo> findPhotosByAlbumId(@Param("albumId") Long albumId, Pageable pageable);

    // 특정 앨범에서 특정 사진이 존재하는지 확인
    boolean existsByAlbumIdAndPhotoId(Long albumId, Long photoId);

    // 특정 사진 앨범 연결 엔티티 찾기
    Optional<AlbumPhoto> findByAlbumIdAndPhotoId(Long albumId, Long photoId);

    // 특정 사진이 속한 모든 앨범 목록 조회
    List<AlbumPhoto> findByPhotoId(Long photoId);

    List<AlbumPhoto> findByAlbumId(Long albumId);

    void deleteByAlbumId(Long albumId);

    void deleteByPhotoId(Long photoId);
}
