package com.media.file.controller;

import com.media.file.dto.*;
import com.media.file.service.MediaFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Media", description = "미디어 파일 업로드·조회·삭제 API (S3 Presigned URL 방식)")
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaFileService mediaFileService;

    @Operation(
            summary = "업로드 세션 시작",
            description = """
                    S3 Presigned PUT URL을 반환합니다.
                    클라이언트는 반환된 URL로 직접 S3에 PUT 요청하여 파일을 업로드합니다.
                    checksum을 전달하면 중복 업로드를 감지합니다.
                    """
    )
    @PostMapping("/upload/init")
    public ResponseEntity<UploadInitResponse> initUpload(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody UploadInitRequest request
    ) {
        return ResponseEntity.ok(mediaFileService.initUpload(memberId, request));
    }

    @Operation(
            summary = "업로드 완료 통보",
            description = """
                    S3 PUT 완료 후 서버에 알립니다.
                    status가 UPLOADING → READY로 전환되고 media.uploaded 이벤트가 발행됩니다.
                    이미지 파일이면 썸네일·WebP 변환이 비동기로 시작됩니다.
                    """
    )
    @PostMapping("/upload/complete/{mediaFileId}")
    public ResponseEntity<MediaFileResponse> completeUpload(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long mediaFileId,
            @RequestBody UploadCompleteRequest request
    ) {
        return ResponseEntity.ok(mediaFileService.completeUpload(memberId, mediaFileId, request));
    }

    @Operation(summary = "파일 메타데이터 조회", description = "변환본(variant) 목록도 함께 반환합니다.")
    @GetMapping("/{mediaFileId}")
    public ResponseEntity<MediaFileResponse> getMediaFile(@PathVariable Long mediaFileId) {
        return ResponseEntity.ok(mediaFileService.getMediaFile(mediaFileId));
    }

    @Operation(
            summary = "파일 논리 삭제",
            description = "status=DELETED로 변경합니다. S3 실제 삭제는 배치 처리됩니다."
    )
    @DeleteMapping("/{mediaFileId}")
    public ResponseEntity<Void> deleteMediaFile(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long mediaFileId
    ) {
        mediaFileService.deleteMediaFile(memberId, mediaFileId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "PRIVATE 파일 임시 다운로드 URL 발급",
            description = "PRIVATE 파일에 대한 Presigned GET URL을 반환합니다. PUBLIC 파일은 CDN을 통해 직접 접근하세요."
    )
    @GetMapping("/{mediaFileId}/presigned-download")
    public ResponseEntity<PresignedDownloadResponse> getPresignedDownloadUrl(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long mediaFileId
    ) {
        return ResponseEntity.ok(mediaFileService.getPresignedDownloadUrl(memberId, mediaFileId));
    }
}
