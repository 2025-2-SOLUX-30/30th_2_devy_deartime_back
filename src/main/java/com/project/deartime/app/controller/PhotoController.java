package com.project.deartime.app.controller;

import com.project.deartime.app.dto.albums.AlbumPhotoRequest;
import com.project.deartime.app.dto.albums.AlbumPhotoResponse;
import com.project.deartime.app.dto.photos.*;
import com.project.deartime.global.dto.PageResponse;
import com.project.deartime.global.exception.SuccessCode;
import com.project.deartime.app.service.PhotoService;
import com.project.deartime.global.dto.ApiResponseTemplete;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    private Long getCurrentUserId(Long principalId) {
        if (principalId == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        return principalId;
    }

    // 사진 업로드 (POST /api/photos)
    @PostMapping(value = "/api/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseTemplete<List<PhotoUploadResponse>>> uploadPhotos(
            @RequestPart(name = "files") List<MultipartFile> files,
            @RequestPart(name = "request") @Valid PhotoUploadRequest request,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);

        PhotoUploadRequest finalRequest = new PhotoUploadRequest(
                files,
                request.caption(),
                request.albumId()
        );

        List<PhotoUploadResponse> response = photoService.uploadPhotos(userId, finalRequest);

        return ApiResponseTemplete.success(SuccessCode.PHOTO_UPLOAD_SUCCESS, response);
    }

    // 사진 목록 조회 (GET /api/photos)
    @GetMapping("/api/photos")
    public ResponseEntity<ApiResponseTemplete<PageResponse<PhotoListResponse>>> getPhotos(
            @AuthenticationPrincipal Long principalId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getCurrentUserId(principalId);
        PageResponse<PhotoListResponse> response = photoService.getPhotos(userId, pageable);

        SuccessCode successCode = response.totalElements() == 0 ?
                SuccessCode.PHOTO_LIST_EMPTY : SuccessCode.PHOTO_LIST_FETCH_SUCCESS;

        return ApiResponseTemplete.success(successCode, response);
    }

    // 사진 캡션 수정 (POST /api/photos/{photoId}/caption)
    @PostMapping("/api/photos/{photoId}/caption")
    public ResponseEntity<ApiResponseTemplete<PhotoDetailResponse>> updatePhotoCaption(
            @PathVariable Long photoId,
            @RequestBody @Valid PhotoCaptionUpdateRequest request,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        PhotoDetailResponse response = photoService.updatePhotoCaption(userId, photoId, request);

        return ApiResponseTemplete.success(SuccessCode.PHOTO_CAPTION_UPDATE_SUCCESS, response);
    }

    // 사진 삭제 (DELETE /api/photos/{photoId})
    @DeleteMapping("/api/photos/{photoId}")
    public ResponseEntity<ApiResponseTemplete<Void>> deletePhoto(
            @PathVariable Long photoId,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        photoService.deletePhoto(userId, photoId);

        return ApiResponseTemplete.success(SuccessCode.PHOTO_DELETE_SUCCESS, null);
    }

    // 앨범에 사진 추가 (POST /api/albums/{albumId}/photos)
    @PostMapping("/api/albums/{albumId}/photos")
    public ResponseEntity<ApiResponseTemplete<List<AlbumPhotoResponse>>> addPhotosToAlbum(
            @PathVariable Long albumId,
            @RequestBody @Valid AlbumPhotoRequest request,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        List<AlbumPhotoResponse> response = photoService.addPhotosToAlbum(userId, albumId, request);

        return ApiResponseTemplete.success(SuccessCode.ALBUM_PHOTO_ADD_SUCCESS, response);
    }

    // 앨범에서 사진 제거 (DELETE /api/albums/{albumId}/photos/{photoId})
    @DeleteMapping("/api/albums/{albumId}/photos/{photoId}")
    public ResponseEntity<ApiResponseTemplete<Void>> removePhotoFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long photoId,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        photoService.removePhotoFromAlbum(userId, albumId, photoId);

        return ApiResponseTemplete.success(SuccessCode.ALBUM_PHOTO_REMOVE_SUCCESS, null);
    }
}
