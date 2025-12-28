package com.project.deartime.app.service;

import com.project.deartime.app.auth.repository.UserRepository;
import com.project.deartime.app.domain.*;
import com.project.deartime.app.dto.*;
import com.project.deartime.app.repository.LetterFavoriteRepository;
import com.project.deartime.app.repository.LetterRepository;
import com.project.deartime.app.repository.LetterThemeRepository;

import com.project.deartime.global.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new EntityNotFoundException("수신자 ID를 찾을 수 없습니다: " + request.receiverId()));

        LetterTheme theme = null;
        String requestedThemeCode = request.theme();
        String warningMessage = null;

        if (requestedThemeCode != null) {
            Optional<LetterTheme> themeOptional = letterThemeRepository.findByCode(requestedThemeCode);

            if (themeOptional.isPresent()) {
                theme = themeOptional.get();
            } else {
                warningMessage = String.format("요청하신 테마 코드 '%s'를 찾을 수 없어 'DEFAULT' 테마로 대체하여 저장됩니다.", requestedThemeCode);
            }
        } else {
            warningMessage = "테마를 지정하지 않아 'DEFAULT' 테마로 저장됩니다.";
        }

        if (theme == null) {
            theme = letterThemeRepository.findByCode("DEFAULT")
                    .orElseThrow(() -> new EntityNotFoundException("기본 테마(DEFAULT)를 찾을 수 없습니다. DB를 확인해주세요."));
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
                "편지가 성공적으로 발송되었습니다.",
                warningMessage
        );
    }

    // 받은 편지 모아보기(GET /api/letters/received)
    @Transactional(readOnly = true)
    public PageResponse<LetterListResponse> getReceivedLetters(Long userId, Pageable pageable) {
        Page<Letter> letterPage = letterRepository.findByReceiverIdAndIsDeletedByReceiverFalse(userId, pageable);
        Page<LetterListResponse> responsePage = letterPage.map(letter -> mapToLetterListResponse(letter, userId));

        return PageResponse.from(responsePage);
    }

    // 보낸 편지 모아보기(GET /api/letters/sent)
    @Transactional(readOnly = true)
    public PageResponse<LetterListResponse> getSentLetters(Long userId, Pageable pageable){
        Page<Letter> letterPage = letterRepository.findBySenderIdAndIsDeletedBySenderFalse(userId, pageable);
        Page<LetterListResponse> responsePage = letterPage.map(letter -> mapToLetterListResponse(letter, userId));

        return PageResponse.from(responsePage);
    }

    // 즐겨찾기 한 편지 모아보기(GET /api/letters/bookmarked)
    @Transactional(readOnly = true)
    public PageResponse<LetterListResponse> getBookmarkedLetters(Long userId, Pageable pageable) {
        Page<Letter> letterPage = letterFavoriteRepository.findBookmarkedLettersByUserId(userId, pageable);
        Page<LetterListResponse> responsePage = letterPage.map(letter -> LetterListResponse.fromEntity(letter, true));

        return PageResponse.from(responsePage);
    }

    // 특정인과 주고받은 편지(GET /api/letters/conversation?targetId={targetId})
    @Transactional(readOnly = true)
    public PageResponse<LetterListResponse> getConversationLetters(Long currentUserId, Long targetUserId, Pageable pageable) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("대상 사용자를 찾을 수 없습니다: " + targetUserId));

        Page<Letter> letterPage = letterRepository.findConversationLetters(currentUserId, targetUserId, pageable);
        Page<LetterListResponse> responsePage = letterPage.map(letter -> mapToLetterListResponse(letter, currentUserId));

        return PageResponse.from(responsePage);
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
        } else {
            letter.softDeleteByReceiver();
        }

        if (letter.isPermanentlyDeletable()) {
            letterRepository.delete(letter);
        }
    }
}
