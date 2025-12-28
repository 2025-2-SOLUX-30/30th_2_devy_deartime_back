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

    /**
     * 앨범 생성
     * POST /api/albums
     */
    @PostMapping
    public ResponseEntity<ApiResponseTemplete<AlbumDetailResponse>> createAlbum(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid AlbumCreateRequest request
    ) {
        Long myId = Long.parseLong(userId);

        AlbumDetailResponse response =
                photoService.createAlbum(myId, request);

        return ApiResponseTemplete.success(
                SuccessCode.ALBUM_CREATE_SUCCESS,
                response
        );
    }

    /**
     * 앨범 목록 조회
     * GET /api/albums
     */
    @GetMapping
    public ResponseEntity<ApiResponseTemplete<List<AlbumListResponse>>> getAlbums(
            @AuthenticationPrincipal String userId
    ) {
        Long myId = Long.parseLong(userId);

        List<AlbumListResponse> response =
                photoService.getAlbums(myId);

        SuccessCode successCode = response.isEmpty()
                ? SuccessCode.ALBUM_LIST_EMPTY
                : SuccessCode.ALBUM_LIST_FETCH_SUCCESS;

        return ApiResponseTemplete.success(
                successCode,
                response
        );
    }

    /**
     * 앨범 이름 수정
     * POST /api/albums/{albumId}/title
     */
    @PostMapping("/{albumId}/title")
    public ResponseEntity<ApiResponseTemplete<AlbumDetailResponse>> updateAlbumTitle(
            @AuthenticationPrincipal String userId,
            @PathVariable Long albumId,
            @RequestBody @Valid AlbumTitleUpdateRequest request
    ) {
        Long myId = Long.parseLong(userId);

        AlbumDetailResponse response =
                photoService.updateAlbumTitle(myId, albumId, request);

        return ApiResponseTemplete.success(
                SuccessCode.ALBUM_TITLE_UPDATE_SUCCESS,
                response
        );
    }

    /**
     * 앨범 삭제
     * DELETE /api/albums/{albumId}
     */
    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponseTemplete<Void>> deleteAlbum(
            @AuthenticationPrincipal String userId,
            @PathVariable Long albumId
    ) {
        Long myId = Long.parseLong(userId);

        photoService.deleteAlbum(myId, albumId);

        return ApiResponseTemplete.success(
                SuccessCode.ALBUM_DELETE_SUCCESS,
                null
        );
    }

    /**
     * 앨범 내 사진 목록 조회
     * GET /api/albums/{albumId}/photos
     */
    @GetMapping("/{albumId}/photos")
    public ResponseEntity<ApiResponseTemplete<PageResponse<PhotoListResponse>>> getPhotosInAlbum(
            @AuthenticationPrincipal String userId,
            @PathVariable Long albumId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long myId = Long.parseLong(userId);

        PageResponse<PhotoListResponse> response =
                photoService.getPhotosInAlbum(myId, albumId, pageable);

        SuccessCode successCode = response.totalElements() == 0
                ? SuccessCode.ALBUM_PHOTOS_EMPTY
                : SuccessCode.ALBUM_PHOTOS_FETCH_SUCCESS;

        return ApiResponseTemplete.success(
                successCode,
                response
        );
    }
}
