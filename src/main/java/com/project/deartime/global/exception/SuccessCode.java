package com.project.deartime.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode {
    // 200 OK
    LOGIN_USER_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다"),
    LETTER_WRITE_INFO_SUCCESS(HttpStatus.OK, "편지 작성화면 정보를 불러왔습니다."),
    GET_LETTER_SUCCESS(HttpStatus.OK, "편지(들)을 불러오는데 성공했습니다"),
    CONVERSATION_EMPTY(HttpStatus.OK, "대화 기록이 없습니다."),
    CONVERSATION_FETCH_SUCCESS(HttpStatus.OK, "대화 기록을 불러오는데 성공했습니다."),
    PHOTO_FETCH_SUCCESS(HttpStatus.OK, "사진(들)을 불러오는데 성공했습니다."),
    ALBUM_CREATE_SUCCESS(HttpStatus.OK, "앨범 생성에 성공했습니다."),
    ALBUM_LIST_FETCH_SUCCESS(HttpStatus.OK, "앨범(들)을 불러오는데 성공했습니다."),
    ALBUM_TITLE_UPDATE_SUCCESS(HttpStatus.OK, "앨범 제목 수정에 성공했습니다."),
    ALBUM_PHOTO_ADD_SUCCESS(HttpStatus.OK, "앨범에 사진 추가에 성공했습니다."),
    ALBUM_PHOTOS_FETCH_SUCCESS(HttpStatus.OK, "앨범의 사진(들)을 불러오는데 성공했습니다."),
    PHOTO_LIST_FETCH_SUCCESS(HttpStatus.OK, "사진 목록을 불러오는데 성공했습니다."),
    PHOTO_LIST_EMPTY(HttpStatus.OK, "사진 목록이 없습니다."),
    PHOTO_CAPTION_UPDATE_SUCCESS(HttpStatus.OK, "사진 캡션 수정에 성공했습니다."),
    ALBUM_PHOTOS_EMPTY(HttpStatus.OK, "앨범에 사진이 없습니다."),
    ALBUM_LIST_EMPTY(HttpStatus.OK, "앨범 목록이 없습니다."),

    // 201 Created, Delete
    LETTER_SEND_SUCCESS(HttpStatus.CREATED, "편지 보내기를 완료하였습니다."),
    DELETE_LETTER_SUCCESS(HttpStatus.NO_CONTENT, "편지가 성공적으로 삭제 되었습니다."),
    PHOTO_UPLOAD_SUCCESS(HttpStatus.CREATED, "사진 업로드에 성공했습니다."),
    PHOTO_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "사진이 성공적으로 삭제 되었습니다."),
    ALBUM_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "앨범이 성공적으로 삭제 되었습니다."),
    ALBUM_PHOTO_REMOVE_SUCCESS(HttpStatus.NO_CONTENT, "앨범에서 사진 제거가 성공적으로 완료되었습니다."),

    // Server
    IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "이미지 업로드 성공"),
    HOME_DATA_RETRIEVED(HttpStatus.OK, "홈화면으로 정보 받아오기 성공");

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode(){
        return httpStatus.value();
    }
}
