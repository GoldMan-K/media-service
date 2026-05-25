package com.media.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    MEDIA_NOT_READY(HttpStatus.BAD_REQUEST, "아직 업로드가 완료되지 않은 파일입니다."),
    MEDIA_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 파일입니다."),
    MEDIA_ACCESS_DENIED(HttpStatus.FORBIDDEN, "파일에 대한 접근 권한이 없습니다."),

    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
    S3_PRESIGN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "다운로드 URL 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
