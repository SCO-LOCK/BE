package com.scolock.BE_SCOLOCK.domain.criteria.entity;

import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "criteria_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_criteria_project_version", columnNames = {"project_id", "version_no"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CriteriaVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "criteria_text", columnDefinition = "longtext")
    private String criteriaText;

    @Builder
    public CriteriaVersion(
            Project project,
            Integer versionNo,
            String criteriaText
    ) {
        this.project = project;
        this.versionNo = versionNo;
        this.criteriaText = criteriaText;
    }
}
