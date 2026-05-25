package com.media.reference.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_reference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mediaFileId;
    private String refType;   // POST | COMMENT | MEETUP
    private Long refId;
    private LocalDateTime createdAt;

    @Builder
    public MediaReference(Long mediaFileId, String refType, Long refId) {
        this.mediaFileId = mediaFileId;
        this.refType = refType;
        this.refId = refId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
