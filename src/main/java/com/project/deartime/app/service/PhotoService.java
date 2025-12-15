package com.project.deartime.app.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.project.deartime.app.domain.*;
import com.project.deartime.app.dto.albums.*;
import com.project.deartime.app.dto.photos.*;
import com.project.deartime.app.repository.AlbumPhotoRepository;
import com.project.deartime.app.repository.AlbumRepository;
import com.project.deartime.app.repository.PhotoRepository;
import com.project.deartime.app.repository.UserRepository;
import com.project.deartime.global.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3Client amazonS3Client;

    private String uploadToS3(MultipartFile file, String uniqueFileName) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(
                new PutObjectRequest(bucket, uniqueFileName, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        return amazonS3Client.getUrl(bucket, uniqueFileName). toString();
    }

    private String extractKeyFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private void deleteFromS3(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        if (amazonS3Client.doesObjectExist(bucket, key)) {
            amazonS3Client.deleteObject(bucket, key);
        }
    }

    // S3에 파일 업로드, DB에 사진 정보 저장 (POST /api/photos)
    public List<PhotoUploadResponse> uploadPhotos(Long userId, PhotoUploadRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        Album targetAlbum = null;
        if (request.albumId() != null) {
            targetAlbum = albumRepository.findById(request.albumId())
                    .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. ID: " + request.albumId()));

            if (!targetAlbum.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("앨범에 대한 접근 권한이 없습니다. ID: " + request.albumId());
            }
        }

        List<PhotoUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file: request.files()) {
            if (file.isEmpty()) continue;

            try {
                String originalFilename = file.getOriginalFilename();
                String extenstion = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".png";
                String uniqueFileName = "photos/" + user.getId() + "/" + UUID.randomUUID().toString();

                String imageUrl = uploadToS3(file, uniqueFileName);

                Photo photo = Photo.builder()
                        .user(user)
                        .imageUrl(imageUrl)
                        .caption(request.caption())
                        .takenAt(LocalDateTime.now())
                        .build();
                Photo savedPhoto = photoRepository.save(photo);

                if (targetAlbum != null) {
                    AlbumPhoto albumPhoto = AlbumPhoto.builder()
                            .album(targetAlbum)
                            .photo(savedPhoto)
                            .build();
                    albumPhotoRepository.save(albumPhoto);
                }

                responses.add(new PhotoUploadResponse(
                        savedPhoto.getId(),
                        savedPhoto.getImageUrl(),
                        savedPhoto.getCaption(),
                        savedPhoto.getTakenAt(),
                        "사진 업로드 및 저장 성공"
                ));
            }
            catch (IOException e) {
                throw new RuntimeException("S3 업로드에 실패하였습니다: " + file.getOriginalFilename(), e);
            }
        }
        return responses;
    }

    // 사진 목록 조회 (GET /api/photos)
    @Transactional(readOnly = true)
    public PageResponse<PhotoListResponse> getPhotos(Long userId, Pageable pageable) {
        Page<Photo> photoPage = photoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        Page<PhotoListResponse> responsePage = photoPage.map(PhotoListResponse::fromEntity);

        return PageResponse.from(responsePage);
    }

    // 사진 캡션 수정 (POST /api/photos/{photoId}/caption)
    public PhotoDetailResponse updatePhotoCaption(Long userId, Long photoId, PhotoCaptionUpdateRequest request) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("사진을 찾을 수 없습니다. ID: " + photoId));

        if (!photo.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("캡션을 수정할 권한이 없습니다. 사진 ID: " + photoId);
        }

        photo.updateCaption(request.caption());
        Photo updatedPhoto = photoRepository.save(photo);

        return PhotoDetailResponse.fromEntity(updatedPhoto);
    }

    // 사진 삭제 (DELETE /api/photos/{photoId})
    public void deletePhoto(Long userId, Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("사진을 찾을 수 없습니다. ID: " + photoId));

        if (!photo.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("사진을 삭제할 권한이 없습니다. 사진 ID: " + photoId);
        }

        deleteFromS3(photo.getImageUrl());

        photoRepository.delete(photo);
    }

    // 앨범 생성 (POST /api/albums)
    public AlbumDetailResponse createAlbum(Long userId, AlbumCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        Album album = Album.builder()
                .user(user)
                .title(request.title())
                .coverUrl(request.coverUrl())
                .build();

        Album savedAlbum = albumRepository.save(album);

        return AlbumDetailResponse.fromEntity(savedAlbum);
    }

    // 앨범 목록 조회 (GET /api/albums)
    @Transactional(readOnly = true)
    public List<AlbumListResponse> getAlbums(Long userId) {
        List<Album> albums = albumRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return albums.stream()
                .map(AlbumListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 앨범 이름 수정 (POST /api/albums/{albumId}/title)
    public AlbumDetailResponse updateAlbumTitle(Long userId, Long albumId, AlbumTitleUpdateRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. ID: " + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범 이름을 수정할 권한이 없습니다. 앨범 ID: " + albumId);
        }

        album.updateTitle(request.title());
        Album updatedAlbum = albumRepository.save(album);

        return AlbumDetailResponse.fromEntity(updatedAlbum);
    }

    // 앨범 삭제 (DELETE /api/albums/{albumId})
    public void deleteAlbum(Long userId, Long albumId, AlbumDeleteRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. ID: " + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범을 삭제할 권한이 없습니다. 앨범 ID: " + albumId);
        }

        if (request.deletePhotos()) {
            // 옵션: 앨범 내의 모든 사진을 영구 삭제
            // AlbumPhoto 레코드들을 조회하여 연결된 Photo 엔티티를 삭제
            List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByAlbumId(albumId);

            for (AlbumPhoto albumPhoto : albumPhotos) {
                Photo photo = albumPhoto.getPhoto();
                deleteFromS3(photo.getImageUrl());
                photoRepository.delete(photo);
            }
        }

        // 앨범 엔티티 삭제
        // request.deletePhotos()가 false인 경우: AlbumPhoto 레코드만 삭제되고 Photo 엔티티는 유지됩니다.
        // request.deletePhotos()가 true인 경우: 모든 Photo와 AlbumPhoto가 위에서 삭제되었으므로, 앨범만 삭제합니다.
        albumRepository.delete(album);
    }

    // 앨범에 사진 추가 (POST /api/albums/{albumId}/photos)
    public List<AlbumPhotoResponse> addPhotosToAlbum(Long userId, Long albumId, AlbumPhotoRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. ID: " + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범에 사진을 추가할 권한이 없습니다. 앨범 ID: " + albumId);
        }

        List<AlbumPhotoResponse> responses = new ArrayList<>();

        for (Long photoId : request.photoIds()) {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(() -> new EntityNotFoundException("사진을 찾을 수 없습니다. ID: " + photoId));

            if (!photo.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("자신의 사진만 앨범에 추가할 수 있습니다. 사진 ID: " + photoId);
            }

            if (!albumPhotoRepository.existsByAlbumIdAndPhotoId(albumId, photoId)) {
                AlbumPhoto albumPhoto = AlbumPhoto.builder()
                        .album(album)
                        .photo(photo)
                        .build();
                AlbumPhoto savedAlbumPhoto = albumPhotoRepository.save(albumPhoto);
                responses.add(AlbumPhotoResponse.fromEntity(savedAlbumPhoto));
            }
        }
        return responses;
    }

    // 앨범에서 사진 목록 조회 (GET /api/albums/{albumId}/photos)
    @Transactional(readOnly = true)
    public PageResponse<PhotoListResponse> getPhotosInAlbum(Long userId, Long albumId, Pageable pageable) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. ID: " + albumId));

        if (!album.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범 사진 목록을 조회할 권한이 없습니다. 앨범 ID: " + albumId);
        }

        Page<Photo> photoPage = albumPhotoRepository.findPhotosByAlbumId(albumId, pageable);

        Page<PhotoListResponse> responsePage = photoPage.map(PhotoListResponse::fromEntity);

        return PageResponse.from(responsePage);
    }

    // 앨범에서 사진 제거 (DELETE /api/albums/{albumId}/photos/{photoId})
    public void removePhotoFromAlbum(Long userId, Long albumId, Long photoId) {
        AlbumPhoto albumPhoto = albumPhotoRepository.findByAlbumIdAndPhotoId(albumId, photoId)
                .orElseThrow(() -> new EntityNotFoundException("앨범에 해당 사진이 연결되어 있지 않습니다."));

        if (!albumPhoto.getAlbum().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("앨범에서 사진을 제거할 권한이 없습니다.");
        }

        albumPhotoRepository.delete(albumPhoto);
    }
}
