package com.project.deartime.app.controller;

import com.project.deartime.app.dto.albums.*;
import com.project.deartime.app.dto.photos.PhotoListResponse;
import com.project.deartime.app.service.PhotoService;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.dto.PageResponse;
import com.project.deartime.global.exception.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final PhotoService photoService;

    private Long getCurrentUserId(Long principalId) {
        if (principalId == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        return principalId;
    }

    // 앨범 생성 (POST /api/albums)
    @PostMapping
    public ResponseEntity<ApiResponseTemplete<AlbumDetailResponse>> createAlbum(
            @RequestBody @Valid AlbumCreateRequest request,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        AlbumDetailResponse response = photoService.createAlbum(userId, request);

        return ApiResponseTemplete.success(SuccessCode.ALBUM_CREATE_SUCCESS, response);
    }

    // 앨범 목록 조회 (GET /api/albums)
    @GetMapping
    public ResponseEntity<ApiResponseTemplete<List<AlbumListResponse>>> getAlbums(
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        List<AlbumListResponse> response = photoService.getAlbums(userId);

        SuccessCode successCode = response.isEmpty() ?
                SuccessCode.ALBUM_LIST_EMPTY : SuccessCode.ALBUM_LIST_FETCH_SUCCESS;

        return ApiResponseTemplete.success(successCode, response);
    }

    // 앨범 이름 수정 (POST /api/albums/{albumId}/title)
    @PostMapping("/{albumId}/title")
    public ResponseEntity<ApiResponseTemplete<AlbumDetailResponse>> updateAlbumTitle(
            @PathVariable Long albumId,
            @RequestBody @Valid AlbumTitleUpdateRequest request,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        AlbumDetailResponse response = photoService.updateAlbumTitle(userId, albumId, request);

        return ApiResponseTemplete.success(SuccessCode.ALBUM_TITLE_UPDATE_SUCCESS, response);
    }

    // 앨범 삭제 (DELETE /api/albums/{albumId})
    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponseTemplete<Void>> deleteAlbum(
            @PathVariable Long albumId,
            @RequestBody @Valid AlbumDeleteRequest request,
            @AuthenticationPrincipal Long principalId
    ) {
        Long userId = getCurrentUserId(principalId);
        photoService.deleteAlbum(userId, albumId, request);

        return ApiResponseTemplete.success(SuccessCode.ALBUM_DELETE_SUCCESS, null);
    }

    // 앨범 내 사진 목록 조회 (GET /api/albums/{albumId}/photos)
    @GetMapping("/{albumId}/photos")
    public ResponseEntity<ApiResponseTemplete<PageResponse<PhotoListResponse>>> getPhotosInAlbum(
            @PathVariable Long albumId,
            @AuthenticationPrincipal Long principalId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = getCurrentUserId(principalId);
        PageResponse<PhotoListResponse> response = photoService.getPhotosInAlbum(userId, albumId, pageable);

        SuccessCode successCode = response.totalElements() == 0 ?
                SuccessCode.ALBUM_PHOTOS_EMPTY : SuccessCode.ALBUM_PHOTOS_FETCH_SUCCESS;

        return ApiResponseTemplete.success(successCode, response);
    }
}
