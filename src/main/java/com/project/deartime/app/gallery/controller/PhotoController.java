package com.project.deartime.app.gallery.controller;

import com.project.deartime.app.gallery.dto.albums.AlbumPhotoRequest;
import com.project.deartime.app.gallery.dto.albums.AlbumPhotoResponse;
import com.project.deartime.app.gallery.dto.photos.*;
import com.project.deartime.app.gallery.service.PhotoService;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.dto.PageResponse;
import com.project.deartime.global.exception.SuccessCode;
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

    /**
     * 사진 업로드
     * POST /api/photos
     */
    @PostMapping(
            value = "/api/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponseTemplete<List<PhotoUploadResponse>>> uploadPhotos(
            @AuthenticationPrincipal String userId,
            @RequestPart(name = "files") List<MultipartFile> files,
            @RequestPart(name = "request", required = false) @Valid PhotoUploadRequest request
    ) {
        Long myId = Long.parseLong(userId);

        PhotoUploadRequest finalRequest = (request == null)
                ? new PhotoUploadRequest(null, null)
                : request;

        List<PhotoUploadResponse> response =
                photoService.uploadPhotos(myId, files, finalRequest);

        return ApiResponseTemplete.success(
                SuccessCode.PHOTO_UPLOAD_SUCCESS,
                response
        );
    }

    /**
     * 사진 목록 조회
     * GET /api/photos
     */
    @GetMapping("/api/photos")
    public ResponseEntity<ApiResponseTemplete<PageResponse<PhotoListResponse>>> getPhotos(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 20, sort = "takenAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long myId = Long.parseLong(userId);

        PageResponse<PhotoListResponse> response =
                photoService.getPhotos(myId, pageable);

        SuccessCode successCode = response.totalElements() == 0
                ? SuccessCode.PHOTO_LIST_EMPTY
                : SuccessCode.PHOTO_LIST_FETCH_SUCCESS;

        return ApiResponseTemplete.success(
                successCode,
                response
        );
    }

    /**
     * 사진 캡션 수정
     * POST /api/photos/{photoId}/caption
     */
    @PostMapping("/api/photos/{photoId}/caption")
    public ResponseEntity<ApiResponseTemplete<PhotoDetailResponse>> updatePhotoCaption(
            @AuthenticationPrincipal String userId,
            @PathVariable Long photoId,
            @RequestBody @Valid PhotoCaptionUpdateRequest request
    ) {
        Long myId = Long.parseLong(userId);

        PhotoDetailResponse response =
                photoService.updatePhotoCaption(myId, photoId, request);

        return ApiResponseTemplete.success(
                SuccessCode.PHOTO_CAPTION_UPDATE_SUCCESS,
                response
        );
    }

    /**
     * 사진 삭제
     * DELETE /api/photos/{photoId}
     */
    @DeleteMapping("/api/photos/{photoId}")
    public ResponseEntity<ApiResponseTemplete<Void>> deletePhoto(
            @AuthenticationPrincipal String userId,
            @PathVariable Long photoId
    ) {
        Long myId = Long.parseLong(userId);

        photoService.deletePhoto(myId, photoId);

        return ApiResponseTemplete.success(
                SuccessCode.PHOTO_DELETE_SUCCESS,
                null
        );
    }

    /**
     * 앨범에 사진 추가
     * POST /api/albums/{albumId}/photos
     */
    @PostMapping("/api/albums/{albumId}/photos")
    public ResponseEntity<ApiResponseTemplete<List<AlbumPhotoResponse>>> addPhotosToAlbum(
            @AuthenticationPrincipal String userId,
            @PathVariable Long albumId,
            @RequestBody @Valid AlbumPhotoRequest request
    ) {
        Long myId = Long.parseLong(userId);

        List<AlbumPhotoResponse> response =
                photoService.addPhotosToAlbum(myId, albumId, request);

        return ApiResponseTemplete.success(
                SuccessCode.ALBUM_PHOTO_ADD_SUCCESS,
                response
        );
    }

    /**
     * 앨범에서 사진 제거
     * DELETE /api/albums/{albumId}/photos/{photoId}
     */
    @DeleteMapping("/api/albums/{albumId}/photos/{photoId}")
    public ResponseEntity<ApiResponseTemplete<Void>> removePhotoFromAlbum(
            @AuthenticationPrincipal String userId,
            @PathVariable Long albumId,
            @PathVariable Long photoId
    ) {
        Long myId = Long.parseLong(userId);

        photoService.removePhotoFromAlbum(myId, albumId, photoId);

        return ApiResponseTemplete.success(
                SuccessCode.ALBUM_PHOTO_REMOVE_SUCCESS,
                null
        );
    }
}
