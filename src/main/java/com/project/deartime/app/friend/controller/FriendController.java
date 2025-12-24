package com.project.deartime.app.friend.controller;

import com.project.deartime.app.friend.dto.FriendSearchResponse;
import com.project.deartime.app.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 친구 닉네임 검색
     * GET /api/friends/search?keyword={닉네임}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFriends(
            @AuthenticationPrincipal String userId,
            @RequestParam("keyword") String keyword
    ) {
        System.out.println("=== 친구 검색 ===");
        System.out.println("userId: " + userId);
        System.out.println("keyword: " + keyword);

        // 검색어가 비어있는 경우 처리
        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 400);
            errorResponse.put("message", "검색어를 입력해주세요.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        List<FriendSearchResponse> searchResults =
                friendService.searchFriendsByNickname(Long.parseLong(userId), keyword.trim());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", 200);
        responseBody.put("message", "검색 성공");
        responseBody.put("count", searchResults.size());
        responseBody.put("results", searchResults);

        return ResponseEntity.ok(responseBody);
    }
}
