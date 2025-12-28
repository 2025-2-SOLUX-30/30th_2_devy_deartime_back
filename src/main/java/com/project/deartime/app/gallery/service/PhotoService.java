package com.project.deartime.app.gallery.service;

import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.*;
import com.project.deartime.app.gallery.ImageMetadataUtil;
import com.project.deartime.app.gallery.dto.albums.*;
import com.project.deartime.app.gallery.dto.photos.*;
import com.project.deartime.app.gallery.repository.AlbumPhotoRepository;
import com.project.deartime.app.gallery.repository.AlbumRepository;
import com.project.deartime.app.gallery.repository.PhotoRepository;

import com.project.deartime.app.service.S3Service;
import com.project.deartime.global.dto.PageResponse;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    /**
     * 사진 업로드
     */
    public List<PhotoUploadResponse> uploadPhotos(
            Long userId,
            List<MultipartFile> files,
            PhotoUploadRequest request
    ) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 사진 파일이 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CoreApiException(ErrorCode.NOT_FOUND_ID_EXCEPTION,
                                "사용자를 찾을 수 없습니다. userId=" + userId)
                );

        Album targetAlbum = null;
        if (request.albumId() != null) {
            targetAlbum = albumRepository.findById(request.albumId())
                    .orElseThrow(() ->
                            new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                                    "앨범을 찾을 수 없습니다. userId=" + request.albumId())
                    );

            if (!targetAlbum.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("앨범에 대한 접근 권한이 없습니다.");
            }
        }

        List<PhotoUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            try{
                byte[] fileBytes = file.getBytes();
                LocalDateTime takenAt = ImageMetadataUtil.extractTakenAt(fileBytes, file.getOriginalFilename());

                if (takenAt == null) {
                    log.warn("사진 촬영 일시 메타데이터가 없습니다. 파일명: {}", file.getOriginalFilename());
                    takenAt = LocalDateTime.now();
                }
                String imageUrl =
                        s3Service.uploadFile(file, "photos/" + user.getId());

                Photo photo = Photo.builder()
                        .user(user)
                        .imageUrl(imageUrl)
                        .caption(request.caption())
                        .takenAt(takenAt)
                        .build();

                Photo savedPhoto = photoRepository.save(photo);

                if (targetAlbum != null) {
                    albumPhotoRepository.save(
                            AlbumPhoto.builder()
                                    .album(targetAlbum)
                                    .photo(savedPhoto)
                                    .build()
                    );
                }

                responses.add(
                        new PhotoUploadResponse(
                                savedPhoto.getId(),
                                savedPhoto.getImageUrl(),
                                savedPhoto.getCaption(),
                                savedPhoto.getTakenAt(),
                                "사진 업로드 및 저장 성공"
                        )
                );
            } catch (IOException e){
                log.error("파일 처리 중 오류 발생: {}", file.getOriginalFilename(), e);
            }
        }

        return responses;
    }


    /**
     * 사진 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<PhotoListResponse> getPhotos(Long userId, Pageable pageable) {
        Page<Photo> photoPage =
                photoRepository.findByUserId(userId, pageable);

        return PageResponse.from(photoPage.map(PhotoListResponse::fromEntity));
    }

    /**
     * 사진 캡션 수정
     */
    public PhotoDetailResponse updatePhotoCaption(Long userId, Long photoId, PhotoCaptionUpdateRequest request) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "사진을 찾을 수 없습니다. photoId=" + photoId));

        if (!photo.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("캡션 수정 권한이 없습니다.");
        }

        photo.updateCaption(request.caption());
        return PhotoDetailResponse.fromEntity(photoRepository.save(photo));
    }

    /**
     * 사진 삭제
     */
    public void deletePhoto(Long userId, Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "사진을 찾을 수 없습니다. photoId=" + photoId));

        if (!photo.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("사진 삭제 권한이 없습니다.");
        }

        // S3 삭제 먼저 시도
        try {
            s3Service.deleteFile(photo.getImageUrl());
        } catch (CoreApiException e) {
            log.error(
                    "[PHOTO DELETE] S3 삭제 실패. photoId={}, userId={}",
                    photoId,
                    userId,
                    e
            );
            throw e;
        }

        // 앨범 커버 해제
        albumRepository.clearCoverPhoto(photoId);

        // AlbumPhoto 관계 제거
        albumPhotoRepository.deleteByPhotoId(photoId);

        // Photo 삭제
        photoRepository.delete(photo);

        log.info(
                "[PHOTO DELETE] 사진 삭제 완료. photoId={}, userId={}",
                photoId,
                userId
        );
    }

    /**
     * 앨범 생성
     */
    public AlbumDetailResponse createAlbum(Long userId, AlbumCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.NOT_FOUND_ID_EXCEPTION,
                        "사용자를 찾을 수 없습니다. userId=" + userId));

        Photo coverPhoto = null;

        if (request.coverPhotoId() != null) {
            coverPhoto = photoRepository.findById(request.coverPhotoId())
                    .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                            "커버 사진을 찾을 수 없습니다. photoId=" + request.coverPhotoId()));

            if (!coverPhoto.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("커버 사진 권한이 없습니다.");
            }
        }

        Album album = albumRepository.save(
                Album.builder()
                        .user(user)
                        .title(request.title())
                        .coverPhoto(coverPhoto)
                        .build()
        );

        if (coverPhoto != null) {
            albumPhotoRepository.save(
                    AlbumPhoto.builder()
                            .album(album)
                            .photo(coverPhoto)
                            .build()
            );
        }

        return AlbumDetailResponse.fromEntity(album);
    }

    /**
     * 앨범 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AlbumListResponse> getAlbums(Long userId) {
        return albumRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AlbumListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 앨범 이름 수정
     */
    public AlbumDetailResponse updateAlbumTitle(Long userId, Long albumId, AlbumTitleUpdateRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "앨범을 찾을 수 없습니다. albumId=" + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범 수정 권한이 없습니다.");
        }

        album.updateTitle(request.title());
        return AlbumDetailResponse.fromEntity(albumRepository.save(album));
    }

    /**
     * 앨범 삭제
     */
    public void deleteAlbum(Long userId, Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "앨범을 찾을 수 없습니다. albumId=" + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범 삭제 권한이 없습니다.");
        }

        albumPhotoRepository.deleteByAlbumId(albumId);
        albumRepository.delete(album);
    }

    /**
     * 앨범에 사진 추가
     */
    public List<AlbumPhotoResponse> addPhotosToAlbum(Long userId, Long albumId, AlbumPhotoRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "앨범을 찾을 수 없습니다. albumId=" + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("사진 추가 권한이 없습니다.");
        }

        List<AlbumPhotoResponse> responses = new ArrayList<>();

        for (Long photoId : request.photoIds()) {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                            "사진을 찾을 수 없습니다. photoId=" + photoId));

            if (!photo.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("본인 사진만 추가할 수 있습니다.");
            }

            if (!albumPhotoRepository.existsByAlbumIdAndPhotoId(albumId, photoId)) {
                responses.add(
                        AlbumPhotoResponse.fromEntity(
                                albumPhotoRepository.save(
                                        AlbumPhoto.builder()
                                                .album(album)
                                                .photo(photo)
                                                .build()
                                )
                        )
                );
            }
        }
        return responses;
    }

    /**
     * 앨범 내 사진 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<PhotoListResponse> getPhotosInAlbum(Long userId, Long albumId, Pageable pageable) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "앨범을 찾을 수 없습니다. albumId=" + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("조회 권한이 없습니다.");
        }

        return PageResponse.from(
                albumPhotoRepository.findPhotosByAlbumId(albumId, pageable)
                        .map(PhotoListResponse::fromEntity)
        );
    }

    /**
     * 앨범에서 사진 제거
     */
    public void removePhotoFromAlbum(Long userId, Long albumId, Long photoId) {
        AlbumPhoto albumPhoto = albumPhotoRepository
                .findByAlbumIdAndPhotoId(albumId, photoId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "앨범에 해당 사진이 없습니다. albumId=" + albumId + ", photoId=" + photoId));

        if (!albumPhoto.getAlbum().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("제거 권한이 없습니다.");
        }

        albumPhotoRepository.delete(albumPhoto);
    }
}
