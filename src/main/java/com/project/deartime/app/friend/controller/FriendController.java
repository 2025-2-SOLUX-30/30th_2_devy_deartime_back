package com.project.deartime.app.friend.controller;

import com.project.deartime.app.friend.dto.*;
import com.project.deartime.app.friend.service.FriendService;
import com.project.deartime.global.dto.ApiResponseTemplete;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import com.project.deartime.global.exception.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 내 친구 목록 조회
     * GET /api/friends
     */
    @GetMapping
    public ResponseEntity<ApiResponseTemplete<FriendListResponse>> getMyFriends(
            @AuthenticationPrincipal String userId
    ) {
        List<FriendResponseDto> friends =
                friendService.getMyFriends(Long.parseLong(userId));

        FriendListResponse response = FriendListResponse.builder()
                .count(friends.size())
                .friends(friends)
                .build();

        return ApiResponseTemplete.success(
                SuccessCode.FRIEND_LIST_SUCCESS,
                response
        );
    }

    /**
     * 친구 닉네임 검색
     * GET /api/friends/search?keyword={닉네임}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseTemplete<FriendSearchListResponse>> searchFriends(
            @AuthenticationPrincipal String userId,
            @RequestParam("keyword") String keyword
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CoreApiException(ErrorCode.SEARCH_KEYWORD_EMPTY);
        }

        List<FriendSearchResponse> searchResults =
                friendService.searchFriendsByNickname(
                        Long.parseLong(userId),
                        keyword.trim()
                );

        FriendSearchListResponse response = FriendSearchListResponse.builder()
                .count(searchResults.size())
                .results(searchResults)
                .build();

        return ApiResponseTemplete.success(
                SuccessCode.FRIEND_SEARCH_SUCCESS,
                response
        );
    }

    /**
     * 친구 추가 요청
     * POST /api/friends
     */
    @PostMapping
    public ResponseEntity<ApiResponseTemplete<FriendResponseDto>> sendFriendRequest(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid FriendRequestDto request
    ) {
        FriendResponseDto friendResponse =
                friendService.sendFriendRequest(
                        Long.parseLong(userId),
                        request.getFriendId()
                );

        return ApiResponseTemplete.success(
                SuccessCode.FRIEND_REQUEST_SUCCESS,
                friendResponse
        );
    }

    /**
     * 친구 관계 상태 변경 (수락/거절/차단)
     * PUT /api/friends/{friendId}
     * Body: { "status": "accepted" | "rejected" | "blocked" }
     */
    @PutMapping("/{friendId}")
    public ResponseEntity<ApiResponseTemplete<Void>> updateFriendStatus(
            @AuthenticationPrincipal String userId,
            @PathVariable Long friendId,
            @RequestBody @Valid FriendStatusUpdateDto request
    ) {
        String status = request.getStatus();

        switch (status) {
            case "accepted":
                friendService.acceptFriendRequest(
                        Long.parseLong(userId),
                        friendId
                );
                return ApiResponseTemplete.success(
                        SuccessCode.FRIEND_REQUEST_ACCEPT_SUCCESS,
                        null
                );

            case "rejected":
                friendService.rejectFriendRequest(
                        Long.parseLong(userId),
                        friendId
                );
                return ApiResponseTemplete.success(
                        SuccessCode.FRIEND_REQUEST_REJECT_SUCCESS,
                        null
                );

            case "blocked":
                friendService.blockFriend(
                        Long.parseLong(userId),
                        friendId
                );
                return ApiResponseTemplete.success(
                        SuccessCode.FRIEND_BLOCK_SUCCESS,
                        null
                );

            default:
                throw new CoreApiException(ErrorCode.INVALID_FRIEND_STATUS);
        }
    }

    /**
     * 친구 삭제
     * DELETE /api/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponseTemplete<Void>> deleteFriend(
            @AuthenticationPrincipal String userId,
            @PathVariable Long friendId
    ) {
        friendService.deleteFriend(
                Long.parseLong(userId),
                friendId
        );

        return ApiResponseTemplete.success(
                SuccessCode.FRIEND_DELETE_SUCCESS,
                null
        );
    }

    /**
     * 대리인 설정
     * PUT /api/friends/{friendId}/proxy
     */
    @PutMapping("/{friendId}/proxy")
    public ResponseEntity<ApiResponseTemplete<ProxyResponseDto>> setProxy(
            @AuthenticationPrincipal String userId,
            @PathVariable Long friendId,
            @RequestBody @Valid ProxyRequestDto request
    ) {
        ProxyResponseDto proxyResponse =
                friendService.setProxy(
                        Long.parseLong(userId),
                        friendId,
                        request.getExpiredAt()
                );

        return ApiResponseTemplete.success(
                SuccessCode.PROXY_SET_SUCCESS,
                proxyResponse
        );
    }

    /**
     * 대리인 해제
     * DELETE /api/friends/{friendId}/proxy
     */
    @DeleteMapping("/{friendId}/proxy")
    public ResponseEntity<ApiResponseTemplete<Void>> removeProxy(
            @AuthenticationPrincipal String userId,
            @PathVariable Long friendId
    ) {
        friendService.removeProxy(
                Long.parseLong(userId),
                friendId
        );

        return ApiResponseTemplete.success(
                SuccessCode.PROXY_REMOVE_SUCCESS,
                null
        );
    }
}
