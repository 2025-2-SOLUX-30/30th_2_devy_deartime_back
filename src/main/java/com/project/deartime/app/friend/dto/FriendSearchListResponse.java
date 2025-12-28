package com.project.deartime.app.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 친구 검색 결과 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendSearchListResponse {
    private int count;
    private List<FriendSearchResponse> results;
}