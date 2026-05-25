package com.media.reference.repository;

import com.media.reference.domain.MediaReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaReferenceRepository extends JpaRepository<MediaReference, Long> {

    List<MediaReference> findAllByMediaFileId(Long mediaFileId);

    // 고아 파일 감지: 특정 파일에 대한 참조가 없는지 확인
    boolean existsByMediaFileId(Long mediaFileId);
}
