package com.project.deartime.app.friend.service;

import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.Friend;
import com.project.deartime.app.domain.Proxy;
import com.project.deartime.app.domain.User;
import com.project.deartime.app.friend.dto.FriendResponseDto;
import com.project.deartime.app.friend.dto.FriendSearchResponse;
import com.project.deartime.app.friend.dto.ProxyResponseDto;
import com.project.deartime.app.friend.repository.FriendRepository;
import com.project.deartime.app.friend.repository.ProxyRepository;
import com.project.deartime.global.exception.CoreApiException;
import com.project.deartime.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ProxyRepository proxyRepository;

    /**
     * 내 친구 목록 조회 (accepted 상태만)
     */
    public List<FriendResponseDto> getMyFriends(Long userId) {
        List<Friend> friendships = friendRepository.findAcceptedFriendsByUserId(userId);
        List<FriendResponseDto> friendList = new ArrayList<>();

        for (Friend friendship : friendships) {
            User friendUser = friendship.getUser().getId().equals(userId)
                    ? friendship.getFriend()
                    : friendship.getUser();

            FriendResponseDto dto = FriendResponseDto.builder()
                    .userId(friendship.getUser().getId())
                    .friendId(friendship.getFriend().getId())
                    .friendNickname(friendUser.getNickname())
                    .friendProfileImageUrl(friendUser.getProfileImageUrl())
                    .friendBio(friendUser.getBio())
                    .status(friendship.getStatus())
                    .requestedAt(friendship.getRequestedAt())
                    .build();

            friendList.add(dto);
        }

        return friendList;
    }

    /**
     * 닉네임으로 친구 검색
     */
    public List<FriendSearchResponse> searchFriendsByNickname(Long currentUserId, String keyword) {
        List<User> searchedUsers = userRepository.searchByNickname(keyword, currentUserId);
        List<FriendSearchResponse> responses = new ArrayList<>();

        for (User user : searchedUsers) {
            String friendStatus = determineFriendStatus(currentUserId, user.getId());

            FriendSearchResponse response = FriendSearchResponse.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .bio(user.getBio())
                    .friendStatus(friendStatus)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    /**
     * 친구 추가 요청
     */
    @Transactional
    public FriendResponseDto sendFriendRequest(Long userId, Long friendId) {
        // 1. 본인에게 친구 요청하는 경우
        if (userId.equals(friendId)) {
            throw new CoreApiException(ErrorCode.FRIEND_SELF_REQUEST);
        }

        // 2. 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.USER_NOT_FOUND));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.FRIEND_NOT_FOUND));

        // 3. 이미 친구 관계가 있는지 확인 (양방향)
        List<Friend> existingFriendships = friendRepository.findFriendshipBetween(userId, friendId);

        for (Friend existing : existingFriendships) {
            String status = existing.getStatus();

            if ("accepted".equals(status)) {
                throw new CoreApiException(ErrorCode.FRIEND_ALREADY_EXISTS);
            }

            if ("pending".equals(status)) {
                // 상대방이 나에게 이미 요청을 보낸 경우 -> 자동으로 수락
                if (existing.getUser().getId().equals(friendId) &&
                        existing.getFriend().getId().equals(userId)) {
                    existing = Friend.builder()
                            .user(existing.getUser())
                            .friend(existing.getFriend())
                            .status("accepted")
                            .requestedAt(existing.getRequestedAt())
                            .build();
                    Friend savedFriend = friendRepository.save(existing);
                    return FriendResponseDto.from(savedFriend);
                }

                // 내가 이미 요청을 보낸 경우
                if (existing.getUser().getId().equals(userId) &&
                        existing.getFriend().getId().equals(friendId)) {
                    throw new CoreApiException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
                }
            }

            if ("blocked".equals(status)) {
                throw new CoreApiException(ErrorCode.FRIEND_USER_BLOCKED);
            }
        }

        // 4. 새로운 친구 요청 생성
        Friend newFriendRequest = Friend.builder()
                .user(user)
                .friend(friend)
                .status("pending")
                .requestedAt(LocalDateTime.now())
                .build();

        Friend savedFriend = friendRepository.save(newFriendRequest);

        return FriendResponseDto.from(savedFriend);
    }

    /**
     * 친구 요청 수락
     */
    @Transactional
    public FriendResponseDto acceptFriendRequest(Long userId, Long friendId) {
        // 1. 사용자 확인
        if (userId.equals(friendId)) {
            throw new CoreApiException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 상대방이 보낸 친구 요청 찾기 (friendId -> userId)
        Friend friendRequest = friendRepository.findByUserIdAndFriendId(friendId, userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        // 3. 상태 확인
        if (!"pending".equals(friendRequest.getStatus())) {
            throw new CoreApiException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
        }

        // 4. 상태를 accepted로 변경
        Friend acceptedFriend = Friend.builder()
                .user(friendRequest.getUser())
                .friend(friendRequest.getFriend())
                .status("accepted")
                .requestedAt(friendRequest.getRequestedAt())
                .build();

        Friend savedFriend = friendRepository.save(acceptedFriend);

        return FriendResponseDto.from(savedFriend);
    }

    /**
     * 친구 요청 거절 (삭제)
     */
    @Transactional
    public void rejectFriendRequest(Long userId, Long friendId) {
        // 1. 사용자 확인
        if (userId.equals(friendId)) {
            throw new CoreApiException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 상대방이 보낸 친구 요청 찾기 (friendId -> userId)
        Friend friendRequest = friendRepository.findByUserIdAndFriendId(friendId, userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        // 3. 상태 확인
        if (!"pending".equals(friendRequest.getStatus())) {
            throw new CoreApiException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
        }

        // 4. 친구 요청 삭제
        friendRepository.delete(friendRequest);
    }

    /**
     * 친구 차단
     */
    @Transactional
    public FriendResponseDto blockFriend(Long userId, Long friendId) {
        // 1. 사용자 확인
        if (userId.equals(friendId)) {
            throw new CoreApiException(ErrorCode.FRIEND_SELF_BLOCK);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.USER_NOT_FOUND));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.FRIEND_NOT_FOUND));

        // 2. 기존 관계 삭제 (양방향 모두)
        List<Friend> existingRelations = friendRepository.findFriendshipBetween(userId, friendId);
        friendRepository.deleteAll(existingRelations);

        // 3. 차단 관계 생성 (userId -> friendId)
        Friend blockedFriend = Friend.builder()
                .user(user)
                .friend(friend)
                .status("blocked")
                .requestedAt(LocalDateTime.now())
                .build();

        Friend savedFriend = friendRepository.save(blockedFriend);

        return FriendResponseDto.from(savedFriend);
    }

    /**
     * 친구 삭제
     */
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        // 1. 사용자 확인
        if (userId.equals(friendId)) {
            throw new CoreApiException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 친구 관계 찾기 (양방향)
        List<Friend> friendships = friendRepository.findFriendshipBetween(userId, friendId);

        if (friendships.isEmpty()) {
            throw new CoreApiException(ErrorCode.FRIEND_RELATIONSHIP_NOT_FOUND);
        }

        // 3. 모든 관계 삭제
        friendRepository.deleteAll(friendships);

        // 4. 대리인 관계도 삭제
        List<Proxy> proxies = proxyRepository.findByUserIdAndProxyUserId(userId, friendId);
        proxies.addAll(proxyRepository.findByUserIdAndProxyUserId(friendId, userId));
        proxyRepository.deleteAll(proxies);
    }

    /**
     * 대리인 설정
     */
    @Transactional
    public ProxyResponseDto setProxy(Long userId, Long proxyUserId, LocalDateTime expiredAt) {
        // 1. 사용자 확인
        if (userId.equals(proxyUserId)) {
            throw new CoreApiException(ErrorCode.PROXY_SELF_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.USER_NOT_FOUND));

        User proxyUser = userRepository.findById(proxyUserId)
                .orElseThrow(() -> new CoreApiException(ErrorCode.FRIEND_NOT_FOUND));

        // 2. 친구 관계 확인 (accepted 상태)
        List<Friend> friendships = friendRepository.findFriendshipBetween(userId, proxyUserId);
        boolean isFriend = friendships.stream()
                .anyMatch(f -> "accepted".equals(f.getStatus()));

        if (!isFriend) {
            throw new CoreApiException(ErrorCode.FRIEND_NOT_ACCEPTED);
        }

        // 3. 기존 대리인 관계 확인
        Optional<Proxy> existingProxy = proxyRepository.findByUserIdAndProxyUserId(userId, proxyUserId)
                .stream().findFirst();

        if (existingProxy.isPresent()) {
            // 기존 대리인 정보 업데이트
            Proxy updatedProxy = Proxy.builder()
                    .id(existingProxy.get().getId())
                    .user(user)
                    .proxyUser(proxyUser)
                    .expiredAt(expiredAt)
                    .build();

            Proxy savedProxy = proxyRepository.save(updatedProxy);
            return ProxyResponseDto.from(savedProxy);
        }

        // 4. 새로운 대리인 관계 생성
        Proxy newProxy = Proxy.builder()
                .user(user)
                .proxyUser(proxyUser)
                .expiredAt(expiredAt)
                .build();

        Proxy savedProxy = proxyRepository.save(newProxy);

        return ProxyResponseDto.from(savedProxy);
    }

    /**
     * 대리인 해제
     */
    @Transactional
    public void removeProxy(Long userId, Long proxyUserId) {
        // 1. 사용자 확인
        if (userId.equals(proxyUserId)) {
            throw new CoreApiException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 대리인 관계 찾기
        List<Proxy> proxies = proxyRepository.findByUserIdAndProxyUserId(userId, proxyUserId);

        if (proxies.isEmpty()) {
            throw new CoreApiException(ErrorCode.PROXY_NOT_FOUND);
        }

        // 3. 대리인 관계 삭제
        proxyRepository.deleteAll(proxies);
    }

    /**
     * 두 사용자 간의 친구 관계 상태 확인
     */
    private String determineFriendStatus(Long userId1, Long userId2) {
        Optional<Friend> sentRequest = friendRepository.findByUserIdAndFriendId(userId1, userId2);
        Optional<Friend> receivedRequest = friendRepository.findByUserIdAndFriendId(userId2, userId1);

        if (sentRequest.isPresent()) {
            return sentRequest.get().getStatus();
        }

        if (receivedRequest.isPresent()) {
            String status = receivedRequest.get().getStatus();
            if ("pending".equals(status)) {
                return "received";
            }
            return status;
        }

        return "none";
    }
}