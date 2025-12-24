package com.project.deartime.app.friend.repository;

import com.project.deartime.app.domain.Friend;
import com.project.deartime.app.domain.FriendId;
import com.project.deartime.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {

    // 특정 사용자의 친구 관계 조회 (양방향)
    @Query("SELECT f FROM Friend f WHERE " +
            "(f.user.id = :userId OR f.friend.id = :userId) " +
            "AND f.status = 'accepted'")
    List<Friend> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    // 친구 요청 상태 확인
    @Query("SELECT f FROM Friend f WHERE " +
            "f.user.id = :userId AND f.friend.id = :friendId")
    Friend findByUserIdAndFriendId(@Param("userId") Long userId,
                                   @Param("friendId") Long friendId);
}