package com.project.deartime.app.friend.service;

import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.Friend;
import com.project.deartime.app.domain.User;
import com.project.deartime.app.friend.dto.FriendSearchResponse;
import com.project.deartime.app.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    /**
     * 닉네임으로 친구 검색
     * @param currentUserId 현재 로그인한 사용자 ID
     * @param keyword 검색 키워드
     * @return 검색된 사용자 목록과 친구 상태
     */
    public List<FriendSearchResponse> searchFriendsByNickname(Long currentUserId, String keyword) {
        // 키워드로 사용자 검색 (본인 제외)
        List<User> searchedUsers = userRepository.searchByNickname(keyword, currentUserId);

        // 현재 사용자의 모든 친구 관계 조회
        List<Friend> myFriendships = friendRepository.findAcceptedFriendsByUserId(currentUserId);

        // 검색된 사용자들의 친구 상태 매핑
        return searchedUsers.stream()
                .map(user -> {
                    String friendStatus = determineFriendStatus(currentUserId, user.getId());
                    return FriendSearchResponse.builder()
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .profileImageUrl(user.getProfileImageUrl())
                            .bio(user.getBio())
                            .friendStatus(friendStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 두 사용자 간의 친구 관계 상태 확인
     * @param userId1 사용자1 ID
     * @param userId2 사용자2 ID
     * @return 친구 상태 ("none", "pending", "accepted", "blocked")
     */
    private String determineFriendStatus(Long userId1, Long userId2) {
        // userId1이 userId2에게 보낸 요청
        Friend sentRequest = friendRepository.findByUserIdAndFriendId(userId1, userId2);

        // userId2가 userId1에게 보낸 요청
        Friend receivedRequest = friendRepository.findByUserIdAndFriendId(userId2, userId1);

        if (sentRequest != null) {
            return sentRequest.getStatus(); // "pending", "accepted", "blocked"
        }

        if (receivedRequest != null) {
            if ("pending".equals(receivedRequest.getStatus())) {
                return "received"; // 친구 요청을 받은 상태
            }
            return receivedRequest.getStatus();
        }

        return "none"; // 친구 관계 없음
    }
}