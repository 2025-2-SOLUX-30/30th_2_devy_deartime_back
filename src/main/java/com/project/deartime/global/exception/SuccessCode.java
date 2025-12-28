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
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    LETTER_WRITE_INFO_SUCCESS(HttpStatus.OK, "편지 작성화면 정보를 불러왔습니다."),
    GET_LETTER_SUCCESS(HttpStatus.OK, "편지(들)을 불러오는데 성공했습니다"),
    GET_LETTER_EMPTY(HttpStatus.OK, "편지가 없습니다."),
    CONVERSATION_EMPTY(HttpStatus.OK, "대화 기록이 없습니다."),
    CONVERSATION_FETCH_SUCCESS(HttpStatus.OK, "대화 기록을 불러오는데 성공했습니다."),
    USER_INFO_RETRIEVED(HttpStatus.OK, "조회 성공"),
    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "프로필 업데이트 성공"),
    HOME_DATA_RETRIEVED(HttpStatus.OK, "홈화면으로 정보 받아오기 성공"),
    IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "이미지 업로드 성공"),

    // 201 Created
    LETTER_SEND_SUCCESS(HttpStatus.CREATED, "편지 보내기를 완료하였습니다."),
    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),

    // 204 No Content
    DELETE_LETTER_SUCCESS(HttpStatus.NO_CONTENT, "편지가 성공적으로 삭제 되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode(){
        return httpStatus.value();
    }
}
