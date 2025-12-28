package com.project.deartime.app.letter.repository;

import com.project.deartime.app.domain.Letter;
import com.project.deartime.app.domain.LetterFavorite;
import com.project.deartime.app.domain.LetterFavoriteId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LetterFavoriteRepository extends JpaRepository<LetterFavorite, LetterFavoriteId> {
    // 특정 user_id로 LetterFavorite 리스트 조회, Letter 엔티티를 패치 조인
    @Query("SELECT lf.letter FROM LetterFavorite lf WHERE lf.user.id = :userId")
    Page<Letter> findBookmarkedLettersByUserId(Long userId, Pageable pageable);

    // 편지 즐겨찾기/취소
    boolean existsByUserIdAndLetterId(Long userId, Long letterId);
}
