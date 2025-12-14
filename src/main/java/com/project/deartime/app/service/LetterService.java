package com.project.deartime.app.service;

import com.project.deartime.app.domain.*;
import com.project.deartime.app.dto.LetterDetailResponse;
import com.project.deartime.app.dto.LetterListResponse;
import com.project.deartime.app.dto.LetterSendRequest;
import com.project.deartime.app.dto.LetterSendResponse;
import com.project.deartime.app.repository.LetterFavoriteRepository;
import com.project.deartime.app.repository.LetterRepository;
import com.project.deartime.app.repository.LetterThemeRepository;
import com.project.deartime.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;
    private final LetterThemeRepository letterThemeRepository;
    private final LetterFavoriteRepository letterFavoriteRepository;

    // 즐겨찾기 여부 확인
    private boolean isLetterBookmarked(Long userId, Long letterId) {
        return letterFavoriteRepository.existsByUserIdAndLetterId(userId, letterId);
    }

    // entity -> DTO로 변환
    private LetterListResponse mapToLetterListResponse(Letter letter, Long currentUserId) {
        boolean isBookmarked = isLetterBookmarked(currentUserId, letter.getId());
        return LetterListResponse.fromEntity(letter, isBookmarked);
    }

    // 편지 전송 (POST /api/letters)
    @Transactional
    public LetterSendResponse sendLetter(LetterSendRequest request) {
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new EntityNotFoundException("발신자 ID를 찾을 수 없습니다: " + request.senderId()));
        User receiver = userRepository.findByNickname(request.receiverNickname())
                .orElseThrow(() -> new EntityNotFoundException("수신자 ID를 찾을 수 없습니다: " + request.receiverNickname()));

        LetterTheme theme = null;
        if (request.theme() != null) {
            Optional<LetterTheme> themeOptional = letterThemeRepository.findByCode(request.theme());
            theme = themeOptional.orElse(null);
        }

        Letter letter = Letter.builder()
                .sender(sender)
                .receiver(receiver)
                .theme(theme)
                .title(request.title())
                .content(request.content())
                .build();

        Letter savedLetter = letterRepository.save(letter);

        return new LetterSendResponse(
                savedLetter.getId(),
                savedLetter.getSender().getNickname(),
                savedLetter.getReceiver().getNickname(),
                savedLetter.getCreatedAt(),
                "편지가 성공적으로 예약 또는 발송되었습니다."
        );
    }

    // 받은 편지 모아보기(GET /api/letters/received)
    public List<LetterListResponse> getReceivedLetters(Long userId, Sort sort) {
        List<Letter> letters = letterRepository.findByReceiverIdAndIsDeletedByReceiverFalse(userId, sort);

        return letters.stream()
                .map(letter -> mapToLetterListResponse(letter, userId))
                .toList();
    }

    // 보낸 편지 모아보기(GET /api/letters/sent)
    public List<LetterListResponse> getSentLetters(Long userId, Sort sort){
        List<Letter> letters = letterRepository.findBySenderIdAndIsDeletedBySenderFalse(userId, sort);

        return letters.stream()
                .map(letter -> mapToLetterListResponse(letter, userId))
                .toList();
    }

    // 즐겨찾기 한 편지 모아보기(GET /api/letters/bookmarked)
    public List<LetterListResponse> getBookmarkedLetters(Long userId, Sort sort) {
        List<Letter> letters = letterFavoriteRepository.findBookmarkedLettersByUserId(userId, sort);

        return letters.stream()
                .map(letter -> LetterListResponse.fromEntity(letter, true))
                .toList();
    }

    // 특정인과 주고받은 편지(GET /api/letters/conversation?targetId={targetId})
    public List<LetterListResponse> getConversationLetters(Long currentUserId, Long targetUserId, Sort sort) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("대상 사용자를 찾을 수 없습니다: " + targetUserId));
        List<Letter> letters = letterRepository.findConversationLetters(currentUserId, targetUserId, sort);

        return letters.stream()
                .map(letter -> mapToLetterListResponse(letter, currentUserId))
                .toList();
    }

    // 편지 상세 확인 및 읽음 처리(GET /api/letters/{letterId}
    @Transactional
    public LetterDetailResponse getLetterDetail(Long letterId, Long currentUserId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new EntityNotFoundException("편지를 찾을 수 없습니다: " + letterId));

        if (!letter.getSender().getId().equals(currentUserId) && !letter.getReceiver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("해당 편지에 접근할 권한이 없습니다.");
        }

        if (letter.getReceiver().getId().equals(currentUserId) && !letter.getIsRead()) {
            letter.markAsRead();
        }

        boolean isBookmarked = isLetterBookmarked(currentUserId, letterId);

        return LetterDetailResponse.fromEntity(letter, isBookmarked);
    }

    // 편지 즐겨찾기/취소 (PUT /api/letters/{letterId}/bookmark)
    @Transactional
    public boolean toggleBookmark(Long letterId, Long userId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new EntityNotFoundException("편지를 찾을 수 없습니다: " + letterId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        boolean isSender = letter.getSender().getId().equals(userId);
        boolean isReceiver = letter.getReceiver().getId().equals(userId);

        // 발신자도 수신자도 아닐 경우 접근 거부
        if (!isSender && !isReceiver) {
            throw new AccessDeniedException("해당 편지의 발신자 또는 수신자만 즐겨찾기 등록/취소가 가능합니다.");
        }

        LetterFavoriteId id = new LetterFavoriteId(userId, letterId);

        if (letterFavoriteRepository.existsByUserIdAndLetterId(userId, letterId)) {
            letterFavoriteRepository.deleteById(id);
            return false;
        } else {
            LetterFavorite favorite = LetterFavorite.builder()
                    .user(user)
                    .letter(letter)
                    .build();
            letterFavoriteRepository.save(favorite);
            return true;
        }
    }

    @Transactional
    public void softDeleteOrPermanentlyDelete(Long letterId, Long currentUserId) {

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new EntityNotFoundException("편지를 찾을 수 없습니다: " + letterId));

        if (!letter.getSender().getId().equals(currentUserId) && !letter.getReceiver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("해당 편지를 삭제할 권한이 없습니다.");
        }

        if (letter.getSender().getId().equals(currentUserId)) {
            letter.softDeleteBySender();
        } else if (letter.getReceiver().getId().equals(currentUserId)) {
            letter.softDeleteByReceiver();
        }

        if (letter.isPermanentlyDeletable()) {
            letterRepository.delete(letter);
        }
    }
}
