package com.media.variant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_file_variant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaFileVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mediaFileId;
    private String variantType;  // THUMB | WEBP | SMALL
    private String objectKey;    // S3 키
    private Integer width;
    private Integer height;
    private Long sizeBytes;
    private LocalDateTime createdAt;

    @Builder
    public MediaFileVariant(Long mediaFileId, String variantType,
                             String objectKey, Integer width, Integer height, Long sizeBytes) {
        this.mediaFileId = mediaFileId;
        this.variantType = variantType;
        this.objectKey = objectKey;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
