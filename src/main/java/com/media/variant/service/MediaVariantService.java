package com.media.variant.service;

import com.media.kafka.producer.MediaEventProducer;
import com.media.variant.domain.MediaFileVariant;
import com.media.variant.repository.MediaFileVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaVariantService {

    private final MediaFileVariantRepository variantRepository;
    private final MediaEventProducer eventProducer;

    /**
     * 비동기 변환 처리 (media.uploaded 이벤트 소비 후 호출)
     * - THUMB: 200x200 썸네일
     * - WEBP : WebP 변환
     * - SMALL: 720px 리사이징
     *
     * 실제 이미지 처리는 Thumbnailator / ImageIO 등 라이브러리 연동 필요.
     * 현재는 변환 흐름 골격만 구현.
     */
    @Async("mediaProcessingExecutor")
    @Transactional
    public void processVariantsAsync(Long mediaFileId, String originalObjectKey, String contentType) {
        log.info("[Variant] 변환 시작: mediaFileId={}", mediaFileId);

        processVariant(mediaFileId, originalObjectKey, contentType, "THUMB");
        processVariant(mediaFileId, originalObjectKey, contentType, "WEBP");
        processVariant(mediaFileId, originalObjectKey, contentType, "SMALL");

        log.info("[Variant] 변환 완료: mediaFileId={}", mediaFileId);
    }

    private void processVariant(Long mediaFileId, String originalObjectKey,
                                 String contentType, String variantType) {
        // 이미 처리된 variant 스킵
        if (variantRepository.existsByMediaFileIdAndVariantType(mediaFileId, variantType)) {
            log.debug("[Variant] 이미 처리된 변환본 스킵: mediaFileId={}, type={}", mediaFileId, variantType);
            return;
        }

        try {
            // TODO: 실제 이미지 변환 로직 (Thumbnailator 등 연동)
            // 현재는 objectKey만 생성하여 저장 (변환본 경로 규칙 적용)
            String variantObjectKey = buildVariantObjectKey(originalObjectKey, variantType);

            MediaFileVariant variant = MediaFileVariant.builder()
                    .mediaFileId(mediaFileId)
                    .variantType(variantType)
                    .objectKey(variantObjectKey)
                    .build();
            variantRepository.save(variant);

            // media.processed 이벤트 발행
            eventProducer.publishMediaProcessed(mediaFileId, variantType, variantObjectKey);
            log.info("[Variant] 변환본 저장: mediaFileId={}, type={}", mediaFileId, variantType);

        } catch (Exception e) {
            log.error("[Variant] 변환 실패: mediaFileId={}, type={}, error={}",
                    mediaFileId, variantType, e.getMessage());
        }
    }

    /**
     * 변환본 objectKey 규칙: 원본 경로에 variant 접미사 추가
     * 예) member/uuid.jpg → member/uuid_THUMB.jpg
     */
    private String buildVariantObjectKey(String originalKey, String variantType) {
        int dotIndex = originalKey.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalKey.substring(0, dotIndex) + "_" + variantType
                    + originalKey.substring(dotIndex);
        }
        return originalKey + "_" + variantType;
    }
}
