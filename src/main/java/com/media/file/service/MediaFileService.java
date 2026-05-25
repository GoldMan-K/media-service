package com.media.file.service;

import com.media.file.domain.MediaFile;
import com.media.file.dto.*;
import com.media.file.repository.MediaFileRepository;
import com.media.global.exception.BusinessException;
import com.media.global.exception.ErrorCode;
import com.media.kafka.producer.MediaEventProducer;
import com.media.variant.repository.MediaFileVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaFileService {

    private final MediaFileRepository mediaFileRepository;
    private final MediaFileVariantRepository variantRepository;
    private final S3Service s3Service;
    private final MediaEventProducer eventProducer;

    @Value("${aws.s3.base-url}")
    private String s3BaseUrl;

    // ─── 1단계: 업로드 세션 시작 ─────────────────────────────────────────────

    @Transactional
    public UploadInitResponse initUpload(Long memberId, UploadInitRequest request) {
        // 중복 업로드 감지 (checksum 일치 + READY 상태)
        if (request.checksum() != null) {
            var existing = mediaFileRepository.findByChecksumAndStatus(request.checksum(), "READY");
            if (existing.isPresent()) {
                MediaFile dup = existing.get();
                log.info("[Media] 중복 파일 감지: checksum={}, mediaFileId={}", request.checksum(), dup.getId());
                String presignedUrl = s3Service.generatePresignedPutUrl(dup.getObjectKey(), request.contentType());
                return new UploadInitResponse(dup.getId(), dup.getObjectKey(),
                        presignedUrl, s3Service.getPresignedUrlExpiryMinutes());
            }
        }

        // 새 객체 키 생성 및 DB 저장 (status=UPLOADING)
        String objectKey = s3Service.generateObjectKey(memberId, request.originalName());

        MediaFile mediaFile = MediaFile.builder()
                .ownerMemberId(memberId)
                .objectKey(objectKey)
                .originalName(request.originalName())
                .contentType(request.contentType())
                .sizeBytes(request.sizeBytes())
                .checksum(request.checksum())
                .storageBucket(s3Service.getBucket())
                .accessLevel(request.accessLevel())
                .build();
        mediaFileRepository.save(mediaFile);

        // S3 Presigned PUT URL 발급
        String presignedUrl = s3Service.generatePresignedPutUrl(objectKey, request.contentType());
        log.info("[Media] 업로드 세션 시작: mediaFileId={}, objectKey={}", mediaFile.getId(), objectKey);

        return new UploadInitResponse(mediaFile.getId(), objectKey,
                presignedUrl, s3Service.getPresignedUrlExpiryMinutes());
    }

    // ─── 2단계: 업로드 완료 통보 ─────────────────────────────────────────────

    @Transactional
    public MediaFileResponse completeUpload(Long memberId, Long mediaFileId,
                                            UploadCompleteRequest request) {
        MediaFile mediaFile = findById(mediaFileId);
        mediaFile.validateOwner(memberId);
        mediaFile.markReady(request.width(), request.height());

        // media.uploaded 이벤트 발행 → 자체 소비해서 썸네일 변환 시작
        eventProducer.publishMediaUploaded(
                mediaFile.getId(), mediaFile.getObjectKey(),
                mediaFile.getContentType(), mediaFile.getOwnerMemberId()
        );
        log.info("[Media] 업로드 완료: mediaFileId={}", mediaFileId);

        return MediaFileResponse.of(mediaFile, s3BaseUrl,
                variantRepository.findAllByMediaFileId(mediaFileId));
    }

    // ─── 파일 메타데이터 조회 ────────────────────────────────────────────────

    public MediaFileResponse getMediaFile(Long mediaFileId) {
        MediaFile mediaFile = findById(mediaFileId);
        mediaFile.validateNotDeleted();
        return MediaFileResponse.of(mediaFile, s3BaseUrl,
                variantRepository.findAllByMediaFileId(mediaFileId));
    }

    // ─── 논리 삭제 ───────────────────────────────────────────────────────────

    @Transactional
    public void deleteMediaFile(Long memberId, Long mediaFileId) {
        MediaFile mediaFile = findById(mediaFileId);
        mediaFile.validateOwner(memberId);
        mediaFile.validateNotDeleted();
        mediaFile.delete();
        // S3 실제 삭제는 배치 처리 (@Scheduled)
        log.info("[Media] 논리 삭제: mediaFileId={}", mediaFileId);
    }

    // ─── PRIVATE 파일 Presigned 다운로드 URL 발급 ────────────────────────────

    public PresignedDownloadResponse getPresignedDownloadUrl(Long memberId, Long mediaFileId) {
        MediaFile mediaFile = findById(mediaFileId);
        mediaFile.validateNotDeleted();
        mediaFile.validateReady();

        // PRIVATE 파일은 소유자만 접근
        if (!mediaFile.isPublic()) {
            mediaFile.validateOwner(memberId);
        }

        String presignedUrl = s3Service.generatePresignedGetUrl(mediaFile.getObjectKey());
        return new PresignedDownloadResponse(presignedUrl, s3Service.getPresignedUrlExpiryMinutes());
    }

    // ─── 공통 ────────────────────────────────────────────────────────────────

    private MediaFile findById(Long mediaFileId) {
        return mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));
    }
}
