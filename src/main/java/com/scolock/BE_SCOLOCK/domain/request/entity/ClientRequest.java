package com.scolock.BE_SCOLOCK.domain.request.entity;

import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Entity
@Table(name = "client_requests", indexes = {
        @Index(name = "idx_client_requests_project_created", columnList = "project_id, created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "request_text", nullable = false, columnDefinition = "longtext")
    private String requestText;

    @Builder
    public ClientRequest(
            Project project,
            String requestText
    ) {
        this.project = project;
        this.requestText = requestText;
    }
}
