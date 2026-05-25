CREATE TABLE IF NOT EXISTS media_file (
    id                BIGINT        NOT NULL AUTO_INCREMENT       COMMENT '파일 PK',
    owner_member_id   BIGINT        NOT NULL                      COMMENT '업로드한 member_id',
    object_key        VARCHAR(500)  NOT NULL                      COMMENT 'S3 객체 키',
    original_name     VARCHAR(255)  NOT NULL                      COMMENT '원본 파일명',
    content_type      VARCHAR(100)  NOT NULL                      COMMENT 'MIME 타입',
    size_bytes        BIGINT        NOT NULL                      COMMENT '파일 크기 (bytes)',
    width             INT           NULL                          COMMENT '이미지 가로 (px)',
    height            INT           NULL                          COMMENT '이미지 세로 (px)',
    checksum          VARCHAR(64)   NULL                          COMMENT 'SHA-256 체크섬 (중복 업로드 감지)',
    storage_provider  VARCHAR(20)   NOT NULL DEFAULT 'S3'         COMMENT '스토리지 공급자',
    storage_bucket    VARCHAR(100)  NOT NULL                      COMMENT 'S3 버킷명',
    access_level      VARCHAR(10)   NOT NULL DEFAULT 'PUBLIC'     COMMENT 'PUBLIC|PRIVATE',
    status            VARCHAR(10)   NOT NULL DEFAULT 'UPLOADING'  COMMENT 'UPLOADING|READY|FAILED|DELETED',
    created_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at        DATETIME(3)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_object_key     (object_key),
    KEY idx_file_owner           (owner_member_id),
    KEY idx_file_status          (status),
    KEY idx_file_checksum        (checksum)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='미디어 파일';

CREATE TABLE IF NOT EXISTS media_file_variant (
    id             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '변환본 PK',
    media_file_id  BIGINT        NOT NULL                 COMMENT '원본 파일 FK',
    variant_type   VARCHAR(10)   NOT NULL                 COMMENT 'THUMB|WEBP|SMALL',
    object_key     VARCHAR(500)  NOT NULL                 COMMENT 'S3 객체 키',
    width          INT           NULL,
    height         INT           NULL,
    size_bytes     BIGINT        NULL,
    created_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_object_key           (object_key),
    UNIQUE KEY uq_file_variant_type    (media_file_id, variant_type),
    KEY idx_variant_file               (media_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='미디어 변환본 (썸네일·WebP 등)';

CREATE TABLE IF NOT EXISTS media_reference (
    id             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '참조 PK',
    media_file_id  BIGINT        NOT NULL                 COMMENT '파일 FK',
    ref_type       VARCHAR(15)   NOT NULL                 COMMENT 'POST|COMMENT|MEETUP',
    ref_id         BIGINT        NOT NULL                 COMMENT '참조 대상 ID',
    created_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_ref_file   (media_file_id),
    KEY idx_ref_target (ref_type, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='미디어 파일 참조 (고아 파일 감지용)';
