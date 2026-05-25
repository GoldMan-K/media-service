package com.media.file.dto;

public record UploadCompleteRequest(
        Integer width,   // 이미지인 경우 가로 (px)
        Integer height   // 이미지인 경우 세로 (px)
) {}
