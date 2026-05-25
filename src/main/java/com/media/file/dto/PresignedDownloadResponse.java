package com.media.file.dto;

public record PresignedDownloadResponse(
        String presignedUrl,
        int expiryMinutes
) {}
