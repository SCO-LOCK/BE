package com.scolock.BE_SCOLOCK.domain.project.entity;

import com.scolock.BE_SCOLOCK.domain.project.enums.ProjectStatus;
import com.scolock.BE_SCOLOCK.domain.user.entity.User;
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
@Table(name = "projects", indexes = {
        @Index(name = "idx_projects_owner_created", columnList = "owner_id, created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "client_name", length = 150)
    private String clientName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false)
    private ProjectStatus status;

    @Builder
    public Project(
            User owner,
            String name,
            String clientName,
            String description,
            ProjectStatus status
    ) {
        this.owner = owner;
        this.name = name;
        this.clientName = clientName;
        this.description = description;
        this.status = status == null ? ProjectStatus.ACTIVE : status;
    }
}
