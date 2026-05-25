package com.media.variant.repository;

import com.media.variant.domain.MediaFileVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaFileVariantRepository extends JpaRepository<MediaFileVariant, Long> {

    List<MediaFileVariant> findAllByMediaFileId(Long mediaFileId);

    boolean existsByMediaFileIdAndVariantType(Long mediaFileId, String variantType);
}
