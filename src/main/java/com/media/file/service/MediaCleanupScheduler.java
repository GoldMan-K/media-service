package com.media.file.service;

import com.media.file.domain.MediaFile;
import com.media.file.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaCleanupScheduler {

    private final MediaFileRepository mediaFileRepository;
    private final S3Service s3Service;

    /**
     * DELETED 파일 S3 실제 삭제 배치 (매일 새벽 3시)
     * 논리 삭제 후 실제 S3 객체를 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedFiles() {
        List<MediaFile> deletedFiles = mediaFileRepository.findAllByStatus("DELETED");
        log.info("[Cleanup] DELETED 파일 S3 정리 시작: {}건", deletedFiles.size());

        int successCount = 0;
        for (MediaFile file : deletedFiles) {
            try {
                s3Service.deleteObject(file.getObjectKey());
                successCount++;
            } catch (Exception e) {
                log.error("[Cleanup] S3 삭제 실패: objectKey={}, error={}",
                        file.getObjectKey(), e.getMessage());
            }
        }
        log.info("[Cleanup] S3 정리 완료: {}/{}건 성공", successCount, deletedFiles.size());
    }

    /**
     * FAILED 파일 S3 정리 배치 (매일 새벽 4시)
     * 업로드 실패로 방치된 S3 객체 정리
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupFailedFiles() {
        List<MediaFile> failedFiles = mediaFileRepository.findAllByStatus("FAILED");
        log.info("[Cleanup] FAILED 파일 S3 정리 시작: {}건", failedFiles.size());

        for (MediaFile file : failedFiles) {
            try {
                s3Service.deleteObject(file.getObjectKey());
                mediaFileRepository.delete(file);
            } catch (Exception e) {
                log.error("[Cleanup] FAILED 파일 정리 실패: objectKey={}", file.getObjectKey());
            }
        }
    }
}
