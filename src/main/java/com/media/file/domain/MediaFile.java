package com.media.file.domain;

import com.media.global.exception.BusinessException;
import com.media.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerMemberId;
    private String objectKey;        // S3 키
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String checksum;         // SHA-256 (중복 업로드 감지)
    private String storageProvider;  // S3
    private String storageBucket;
    private String accessLevel;      // PUBLIC | PRIVATE
    private String status;           // UPLOADING | READY | FAILED | DELETED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Builder
    public MediaFile(Long ownerMemberId, String objectKey, String originalName,
                     String contentType, Long sizeBytes, String checksum,
                     String storageBucket, String accessLevel) {
        this.ownerMemberId = ownerMemberId;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.storageProvider = "S3";
        this.storageBucket = storageBucket;
        this.accessLevel = accessLevel != null ? accessLevel : "PUBLIC";
        this.status = "UPLOADING";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드

    public void markReady(Integer width, Integer height) {
        this.status = "READY";
        this.width = width;
        this.height = height;
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public void delete() {
        this.status = "DELETED";
        this.deletedAt = LocalDateTime.now();
    }

    public void validateOwner(Long memberId) {
        if (!this.ownerMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.MEDIA_ACCESS_DENIED);
        }
    }

    public void validateReady() {
        if (!"READY".equals(this.status)) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_READY);
        }
    }

    public void validateNotDeleted() {
        if ("DELETED".equals(this.status)) {
            throw new BusinessException(ErrorCode.MEDIA_ALREADY_DELETED);
        }
    }

    public boolean isPublic() {
        return "PUBLIC".equals(this.accessLevel);
    }
}
