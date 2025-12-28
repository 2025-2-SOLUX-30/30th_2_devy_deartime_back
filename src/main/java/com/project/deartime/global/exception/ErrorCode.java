package com.project.deartime.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    // 400 Bad Request
    LOGIN_USER_FAILED(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다."),
    VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    PASSWORD_MISMATCH_EXCEPTION(HttpStatus.BAD_REQUEST,"비밀번호를 확인해주세요."),
    ALREADY_EXIT_EMAIL_EXCEPTION(HttpStatus.BAD_REQUEST,"이미 존재하는 이메일입니다."),
    ALREADY_EXIT_NICKNAME_EXCEPTION(HttpStatus.BAD_REQUEST,"이미 존재하는 닉네임입니다."),
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "필수적인 요청 값이 입력되지 않았습니다."),
    VALIDATION_REQUEST_HEADER_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 헤더값이 입력되지 않았습니다."),
    VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 파라미터값이 입력되지 않았습니다."),
    REQUEST_METHOD_VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 메소드가 잘못됐습니다."),
    VALIDATION_REQUEST_FAIL_USERINFO_EXCEPTION(HttpStatus.BAD_REQUEST,"사용자 정보를 받아오는데 실패했습니다."),
    VALIDATION_JSON_SYNTAX_FAIL(HttpStatus.BAD_REQUEST, "JSON 파싱 오류 발생"),
    INVALID_ROLE_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "올바르지 않은 권한 요청입니다."),
    INVALID_ID_EXCEPTION(HttpStatus.BAD_REQUEST, "사용자 ID가 유효하지 않습니다. "),
    INVALID_SIGNATURE_EXCEPTION(HttpStatus.BAD_REQUEST, "JWT 토큰의 서명이 올바르지 않습니다."),
    INVALID_DISPLAY_NAME_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 displayName이 있습니다 "),
    NUMBER_LESS_THAN_ZERO_EXCEPTION(HttpStatus.BAD_REQUEST, "페이지의 크기 번호나 페이지의 사이즈는 0 미만일 수 없습니다. "),
    INVALID_FILE_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일입니다."),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "요청 본문에 올바르지 않은 Enum 값이 포함되어 있습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    DB_CONSTRAINT_VIOLATION_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 데이터베이스 제약 조건을 위반했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST,"요청 형식이 잘못되었습니다. 데이터 타입을 확인해주세요."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),

    // Friend 관련 400 에러
    FRIEND_SELF_REQUEST(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    FRIEND_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 친구 관계입니다."),
    FRIEND_REQUEST_ALREADY_SENT(HttpStatus.BAD_REQUEST, "이미 친구 요청을 보냈습니다."),
    FRIEND_USER_BLOCKED(HttpStatus.BAD_REQUEST, "차단된 사용자입니다."),
    FRIEND_REQUEST_NOT_PENDING(HttpStatus.BAD_REQUEST, "대기 중인 친구 요청이 아닙니다."),
    FRIEND_NOT_ACCEPTED(HttpStatus.BAD_REQUEST, "친구 관계가 아닌 사용자는 대리인으로 설정할 수 없습니다."),
    PROXY_SELF_REQUEST(HttpStatus.BAD_REQUEST, "자기 자신을 대리인으로 설정할 수 없습니다."),
    FRIEND_SELF_BLOCK(HttpStatus.BAD_REQUEST, "자기 자신을 차단할 수 없습니다."),
    SEARCH_KEYWORD_EMPTY(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    INVALID_FRIEND_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다."),

    // 401 Unauthorized
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자거나 인증과정에 오류가 있습니다. "),

    // 403 Forbidden
    FORBIDDEN_AUTH_EXCEPTION(HttpStatus.FORBIDDEN, "권한 정보가 없는 토큰입니다."),
    EXPIRED_TOKEN_EXCEPTION(HttpStatus.FORBIDDEN, "토큰이 만료되었습니다."),
    ACCESS_DENIED_EXCEPTION(HttpStatus.FORBIDDEN, "접근 권한이 없습니다. "),
    AUTHENTICATION_FAILED_EXCEPTION(HttpStatus.FORBIDDEN, "인증에 실패했습니다. "),

    // 404 NOT FOUND
    NOT_FOUND_ID_EXCEPTION(HttpStatus.NOT_FOUND, "찾을 수 없는 ID"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "친구를 찾을 수 없습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다."),
    FRIEND_RELATIONSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다."),
    PROXY_NOT_FOUND(HttpStatus.NOT_FOUND, "대리인 관계를 찾을 수 없습니다."),

    // 409 Conflict
    ALREADY_EXIST_STUDENT_EXCEPTION(HttpStatus.CONFLICT, "이미 회원가입이 완료된 사용자입니다."),
    ALREADY_EXIST_SUBJECT_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 제목/리소스를 생성하려고 시도했습니다."),

    // 500 Internal Server Exception
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다."),
    TOKEN_CREATION_FAILED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "토큰을 생성하는 과정에서 알 수 없는 오류가 발생했습니다."),
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "입출력 오류가 발생했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다."),

    // 503 Service Unavailable
    FAILED_GET_TOKEN_EXCEPTION(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, "구글 엑세스 토큰을 가져오는데 실패했습니다."),
    FAILED_UPLOAD_IMAGE_FILE_EXCEPTION(HttpStatus.SERVICE_UNAVAILABLE, "이미지를 업로드하는데 실패했습니다. "),

    // letter
    LETTER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 편지에 접근하거나 조작할 권한이 없습니다."),
    LETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 편지를 찾을 수 없습니다."),
    LETTER_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 편지 테마를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}