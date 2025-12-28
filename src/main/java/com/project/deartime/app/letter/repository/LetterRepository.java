package com.project.deartime.app.letter.repository;

import com.project.deartime.app.domain.Letter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {
    // receiverId로 조회하며, 수신자가 삭제하지 않은 편지만 반환
    Page<Letter> findByReceiverIdAndIsDeletedByReceiverFalse(Long receiverId, Pageable pageable);

    // senderId로 조회하며, 발신자가 삭제하지 않은 편지만 반환
    Page<Letter> findBySenderIdAndIsDeletedBySenderFalse(Long senderId, Pageable pageable);

    // 편지 상세 확인
    Optional<Letter> findById(Long letterId);

    // 특정인과 주고받은 편지
    @Query("SELECT l FROM Letter l " +
           "WHERE (l.sender.id = :user1Id AND l.receiver.id = :user2Id AND l.isDeletedBySender = FALSE) " +
           "OR (l.sender.id = :user2Id AND l.receiver.id = :user1Id AND l.isDeletedByReceiver = FALSE) ")
    Page<Letter> findConversationLetters(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, Pageable pageable);
}
