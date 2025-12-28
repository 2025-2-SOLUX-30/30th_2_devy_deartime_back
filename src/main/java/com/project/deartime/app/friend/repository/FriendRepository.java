package com.project.deartime.app.friend.repository;

import com.project.deartime.app.domain.Friend;
import com.project.deartime.app.domain.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, FriendId> {

    @Query("SELECT f FROM Friend f WHERE " +
            "(f.user.id = :userId OR f.friend.id = :userId) " +
            "AND f.status = 'accepted'")
    List<Friend> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friend f WHERE " +
            "f.user.id = :userId AND f.friend.id = :friendId")
    Optional<Friend> findByUserIdAndFriendId(@Param("userId") Long userId,
                                             @Param("friendId") Long friendId);

    // 양방향 친구 관계 확인 (userId -> friendId 또는 friendId -> userId)
    @Query("SELECT f FROM Friend f WHERE " +
            "(f.user.id = :userId AND f.friend.id = :friendId) OR " +
            "(f.user.id = :friendId AND f.friend.id = :userId)")
    List<Friend> findFriendshipBetween(@Param("userId") Long userId,
                                       @Param("friendId") Long friendId);
}