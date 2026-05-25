package com.media.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_MEDIA_UPLOADED  = "media.uploaded";
    private static final String TOPIC_MEDIA_PROCESSED = "media.processed";

    /**
     * 업로드 완료(READY 전환) 시 발행
     * - 자체 소비: 썸네일·WebP 비동기 변환 시작
     */
    public void publishMediaUploaded(Long mediaFileId, String objectKey,
                                     String contentType, Long ownerMemberId) {
        Map<String, Object> payload = Map.of(
                "mediaFileId", mediaFileId,
                "objectKey", objectKey,
                "contentType", contentType,
                "ownerMemberId", ownerMemberId
        );
        kafkaTemplate.send(TOPIC_MEDIA_UPLOADED, String.valueOf(mediaFileId), payload);
        log.info("[Kafka] media.uploaded published: mediaFileId={}", mediaFileId);
    }

    /**
     * 변환 완료 시 발행
     * - Community·Meetup Service 소비: variant URL 사용 가능 신호
     */
    public void publishMediaProcessed(Long mediaFileId, String variantType, String objectKey) {
        Map<String, Object> payload = Map.of(
                "mediaFileId", mediaFileId,
                "variantType", variantType,
                "objectKey", objectKey
        );
        kafkaTemplate.send(TOPIC_MEDIA_PROCESSED, String.valueOf(mediaFileId), payload);
        log.info("[Kafka] media.processed published: mediaFileId={}, variantType={}", mediaFileId, variantType);
    }
}
