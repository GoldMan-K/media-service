package com.media.file.dto;

import com.media.file.domain.MediaFile;
import com.media.variant.domain.MediaFileVariant;

import java.time.LocalDateTime;
import java.util.List;

public record MediaFileResponse(
        Long id,
        Long ownerMemberId,
        String objectKey,
        String originalName,
        String contentType,
        Long sizeBytes,
        Integer width,
        Integer height,
        String accessLevel,
        String status,
        String fileUrl,           // PUBLIC이면 CDN/S3 직접 URL
        List<VariantDto> variants,
        LocalDateTime createdAt
) {
    public record VariantDto(String variantType, String objectKey,
                              Integer width, Integer height) {
        public static VariantDto from(MediaFileVariant v) {
            return new VariantDto(v.getVariantType(), v.getObjectKey(),
                    v.getWidth(), v.getHeight());
        }
    }

    public static MediaFileResponse of(MediaFile f, String baseUrl,
                                       List<MediaFileVariant> variants) {
        String fileUrl = f.isPublic() ? baseUrl + "/" + f.getObjectKey() : null;
        return new MediaFileResponse(
                f.getId(),
                f.getOwnerMemberId(),
                f.getObjectKey(),
                f.getOriginalName(),
                f.getContentType(),
                f.getSizeBytes(),
                f.getWidth(),
                f.getHeight(),
                f.getAccessLevel(),
                f.getStatus(),
                fileUrl,
                variants.stream().map(VariantDto::from).toList(),
                f.getCreatedAt()
        );
    }
}
