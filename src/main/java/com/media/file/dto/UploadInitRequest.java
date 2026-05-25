package com.media.file.dto;

public record UploadInitRequest(
        String originalName,
        String contentType,
        Long sizeBytes,
        String checksum,    // 클라이언트가 미리 계산한 SHA-256 (선택)
        String accessLevel  // PUBLIC | PRIVATE (기본값: PUBLIC)
) {}
