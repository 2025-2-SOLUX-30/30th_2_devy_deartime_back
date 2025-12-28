package com.project.deartime.global.exception;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.project.deartime.global.dto.ApiResponseTemplete;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE; // 적절한 ErrorCode 사용

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse(errorCode.getMessage());

        return ApiResponseTemplete.error(
                errorCode,
                errorMessage
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResponseTemplete.error(ErrorCode.LETTER_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(CoreApiException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleCoreApiException(CoreApiException e) {
        return ApiResponseTemplete.error(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        ErrorCode errorCode = ErrorCode.INVALID_JSON_FORMAT;
        String detailMessage = errorCode.getMessage();

        if (e.getCause() instanceof MismatchedInputException mismatch) {

            if (mismatch.getTargetType() != null && mismatch.getTargetType().isEnum()) {
                detailMessage = String.format("유효하지 않은 Enum 값입니다. 필드: %s, 예상 타입: %s",
                        mismatch.getPath().get(mismatch.getPath().size() - 1).getFieldName(),
                        mismatch.getTargetType().getSimpleName());

            } else if (mismatch.getTargetType() != null) {
                String fieldName = mismatch.getPath().get(mismatch.getPath().size() - 1).getFieldName();
                String targetType = mismatch.getTargetType().getSimpleName();

                detailMessage = String.format("요청 필드 '%s'의 타입이 잘못되었습니다. 예상 타입: %s",
                        fieldName,
                        targetType);
            } else {
                detailMessage = "JSON 구조 또는 타입이 잘못되었습니다. 요청 본문을 확인해주세요.";
            }
        } else {
            detailMessage = "요청 본문을 읽을 수 없습니다. JSON 형식이 올바른지 확인해주세요.";
        }

        return ApiResponseTemplete.error(errorCode, detailMessage);
    }

    private String extractInvalidStatus(HttpMessageNotReadableException e) {
        String message = e.getMessage();
        if (message.contains("Unknown status")) {
            // 메시지가 "Unknown status: ddd" 형식일 때 "ddd" 부분만 추출
            int startIndex = message.indexOf("Unknown status:") + "Unknown status:".length();
            int endIndex = message.indexOf("\n", startIndex);
            if (endIndex == -1) endIndex = message.length();
            return message.substring(startIndex, endIndex).trim();
        }
        return "Unknown status";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponseTemplete.error(ErrorCode.RESOURCE_NOT_FOUND, "Resource not found: " + e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleMissingParamException(MissingServletRequestParameterException e) {
        return ApiResponseTemplete.error(ErrorCode.VALIDATION_REQUEST_PARAMETER_MISSING_EXCEPTION, e.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorCode errorCode = mapHttpStatusToErrorCode(status);
        return ApiResponseTemplete.error(errorCode, e.getReason());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleInvalidTokenException(InvalidTokenException e) {
        return ApiResponseTemplete.error(ErrorCode.UNAUTHORIZED_EXCEPTION, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e
    ) {
        return ApiResponseTemplete.error(ErrorCode.NOT_SUPPORT_MEDIA_TYPE, e.getMessage());
    }

    // 최종 Fallback 예외 처리

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleGeneralException(Exception e) {
        e.printStackTrace(); // 서버 로그에 스택 트레이스 출력
        return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected server error occurred: " + e.getMessage());
    }

    // HTTP Status Code를 ErrorCode로 변환
    private ErrorCode mapHttpStatusToErrorCode(HttpStatus status) {
        switch (status) {
            case BAD_REQUEST:
                return ErrorCode.INVALID_REQUEST;
            case UNAUTHORIZED:
                return ErrorCode.UNAUTHORIZED_EXCEPTION;
            case FORBIDDEN:
                return ErrorCode.ACCESS_DENIED_EXCEPTION;
            case NOT_FOUND:
                return ErrorCode.RESOURCE_NOT_FOUND;
            case CONFLICT:
                return ErrorCode.ALREADY_EXIST_SUBJECT_EXCEPTION;
            case INTERNAL_SERVER_ERROR:
                return ErrorCode.INTERNAL_SERVER_ERROR;
            default:
                return ErrorCode.UNKNOWN_ERROR;
        }
    }
}
