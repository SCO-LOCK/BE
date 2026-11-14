package com.scolock.BE_SCOLOCK.domain.analysis.entity;

import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisClassification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "analysis_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_analysis_items_order", columnNames = {"analysis_run_id", "item_order"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRun analysisRun;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Column(name = "request_excerpt", nullable = false, columnDefinition = "text")
    private String requestExcerpt;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false)
    private AnalysisClassification classification;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "criteria_evidence", columnDefinition = "text")
    private String criteriaEvidence;

    @Column(name = "suggested_question", columnDefinition = "text")
    private String suggestedQuestion;

    @Builder
    public AnalysisItem(
            AnalysisRun analysisRun,
            Integer itemOrder,
            String requestExcerpt,
            AnalysisClassification classification,
            String reason,
            String criteriaEvidence,
            String suggestedQuestion
    ) {
        this.analysisRun = analysisRun;
        this.itemOrder = itemOrder;
        this.requestExcerpt = requestExcerpt;
        this.classification = classification;
        this.reason = reason;
        this.criteriaEvidence = criteriaEvidence;
        this.suggestedQuestion = suggestedQuestion;
    }
}
