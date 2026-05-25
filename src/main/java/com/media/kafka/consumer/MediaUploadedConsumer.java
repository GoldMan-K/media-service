package com.media.kafka.consumer;

import com.media.variant.service.MediaVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadedConsumer {

    private final MediaVariantService variantService;

    /**
     * media.uploaded 자체 소비
     * - 이미지 파일이면 썸네일·WebP 비동기 변환 시작
     */
    @KafkaListener(topics = "media.uploaded", groupId = "media-service-group")
    public void handleMediaUploaded(Map<String, Object> payload) {
        try {
            Long mediaFileId = Long.valueOf(payload.get("mediaFileId").toString());
            String contentType = payload.getOrDefault("contentType", "").toString();
            String objectKey = payload.get("objectKey").toString();

            log.info("[Kafka] media.uploaded consumed: mediaFileId={}, contentType={}", mediaFileId, contentType);

            // 이미지 파일만 변환 처리
            if (contentType.startsWith("image/")) {
                variantService.processVariantsAsync(mediaFileId, objectKey, contentType);
            }
        } catch (Exception e) {
            log.error("[Kafka] media.uploaded 처리 실패: {}", e.getMessage(), e);
        }
    }
}
