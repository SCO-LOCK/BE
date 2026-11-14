package com.scolock.BE_SCOLOCK.domain.criteria.entity;

import com.scolock.BE_SCOLOCK.domain.criteria.enums.ExtractionStatus;
import com.scolock.BE_SCOLOCK.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "criteria_files", indexes = {
        @Index(name = "idx_criteria_files_version", columnList = "criteria_version_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CriteriaFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criteria_version_id", nullable = false)
    private CriteriaVersion criteriaVersion;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "extraction_status", nullable = false)
    private ExtractionStatus extractionStatus;

    @Column(name = "extracted_text", columnDefinition = "longtext")
    private String extractedText;

    @Builder
    public CriteriaFile(
            CriteriaVersion criteriaVersion,
            String storageKey,
            String originalFilename,
            String mimeType,
            Long fileSizeBytes,
            ExtractionStatus extractionStatus,
            String extractedText
    ) {
        this.criteriaVersion = criteriaVersion;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.extractionStatus = extractionStatus == null ? ExtractionStatus.PENDING : extractionStatus;
        this.extractedText = extractedText;
    }
}
