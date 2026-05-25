package com.media.file.service;

import com.media.global.exception.BusinessException;
import com.media.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presigned-url-expiry-minutes:10}")
    private int presignedUrlExpiryMinutes;

    /**
     * S3 Presigned PUT URL 생성 (업로드용)
     */
    public String generatePresignedPutUrl(String objectKey, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                    .putObjectRequest(putRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("[S3] Presigned PUT URL 생성 실패: objectKey={}, error={}", objectKey, e.getMessage());
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    /**
     * S3 Presigned GET URL 생성 (PRIVATE 파일 다운로드용)
     */
    public String generatePresignedGetUrl(String objectKey) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                    .getObjectRequest(getRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("[S3] Presigned GET URL 생성 실패: objectKey={}, error={}", objectKey, e.getMessage());
            throw new BusinessException(ErrorCode.S3_PRESIGN_FAILED);
        }
    }

    /**
     * S3 객체 삭제
     */
    public void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            log.info("[S3] 객체 삭제 완료: objectKey={}", objectKey);
        } catch (Exception e) {
            log.error("[S3] 객체 삭제 실패: objectKey={}, error={}", objectKey, e.getMessage());
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED);
        }
    }

    /**
     * 고유한 S3 객체 키 생성
     * 형식: {memberId}/{uuid}.{확장자}
     */
    public String generateObjectKey(Long memberId, String originalName) {
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalName.substring(dotIndex); // .jpg, .png 등
        }
        return memberId + "/" + UUID.randomUUID() + ext;
    }

    public String getBucket() {
        return bucket;
    }

    public int getPresignedUrlExpiryMinutes() {
        return presignedUrlExpiryMinutes;
    }
}
