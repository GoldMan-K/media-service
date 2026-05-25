package com.media.file.dto;

public record UploadInitResponse(
        Long mediaFileId,      // DB에 저장된 파일 ID
        String objectKey,      // S3 객체 키
        String presignedUrl,   // S3 Presigned PUT URL (클라이언트가 직접 S3에 업로드)
        int expiryMinutes      // URL 만료 시간 (분)
) {}
