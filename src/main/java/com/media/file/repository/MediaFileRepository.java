package com.media.file.repository;

import com.media.file.domain.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    // 중복 업로드 감지 (checksum 일치 + READY 상태)
    Optional<MediaFile> findByChecksumAndStatus(String checksum, String status);

    // 배치 정리 대상 조회
    List<MediaFile> findAllByStatus(String status);
}
