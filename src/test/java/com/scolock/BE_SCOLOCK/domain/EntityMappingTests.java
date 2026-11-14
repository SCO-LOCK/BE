package com.scolock.BE_SCOLOCK.domain;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisItem;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisClassification;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaFile;
import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import com.scolock.BE_SCOLOCK.domain.criteria.enums.ExtractionStatus;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.enums.ProjectStatus;
import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import com.scolock.BE_SCOLOCK.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityMappingTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAllEntitiesWithRelationshipsDefaultsAuditingAndJson() {
        User user = persist(User.builder().kakaoId(100L).build());
        Project project = persist(Project.builder().owner(user).name("프로젝트").build());
        CriteriaVersion version = persist(CriteriaVersion.builder()
                .project(project).versionNo(1).criteriaText("제작 기준").build());
        CriteriaFile file = persist(CriteriaFile.builder().criteriaVersion(version)
                .storageKey("criteria/spec.pdf").originalFilename("spec.pdf")
                .mimeType("application/pdf").fileSizeBytes(1024L).build());
        ClientRequest request = persist(ClientRequest.builder()
                .project(project).requestText("수정 요청").build());
        String json = "{\"summary\":\"검토 필요\",\"count\":1}";
        AnalysisRun run = persist(AnalysisRun.builder().clientRequest(request)
                .criteriaVersion(version).resultJson(json).build());
        AnalysisItem item = persist(AnalysisItem.builder().analysisRun(run).itemOrder(1)
                .requestExcerpt("수정 요청").classification(AnalysisClassification.NEEDS_REVIEW)
                .reason("상세 확인 필요").build());
        entityManager.flush();
        entityManager.clear();

        AnalysisItem loaded = entityManager.find(AnalysisItem.class, item.getId());
        AnalysisRun loadedRun = loaded.getAnalysisRun();
        assertThat(loaded.getClassification()).isEqualTo(AnalysisClassification.NEEDS_REVIEW);
        assertThat(loadedRun.getResultJson()).isEqualTo(json);
        assertThat(loadedRun.getStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(loadedRun.getClientRequest().getProject().getOwner().getKakaoId()).isEqualTo(100L);
        assertThat(loadedRun.getCriteriaVersion().getId()).isEqualTo(version.getId());
        assertThat(entityManager.find(CriteriaFile.class, file.getId()).getExtractionStatus())
                .isEqualTo(ExtractionStatus.PENDING);
        assertThat(entityManager.find(Project.class, project.getId()).getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(project.getUpdatedAt()).isNotNull();
        assertThat(version.getCreatedAt()).isNotNull();
        assertThat(version.getUpdatedAt()).isNotNull();
        assertThat(file.getCreatedAt()).isNotNull();
        assertThat(file.getUpdatedAt()).isNotNull();
        assertThat(request.getCreatedAt()).isNotNull();
        assertThat(request.getUpdatedAt()).isNotNull();
        assertThat(run.getCreatedAt()).isNotNull();
        assertThat(run.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsInvalidAnalysisStatusWrittenWithSql() {
        User user = persist(User.builder().kakaoId(100L).build());
        Project project = persist(Project.builder().owner(user).name("프로젝트").build());
        CriteriaVersion version = persist(CriteriaVersion.builder().project(project).versionNo(1).build());
        ClientRequest request = persist(ClientRequest.builder().project(project).requestText("요청").build());
        AnalysisRun run = persist(AnalysisRun.builder().clientRequest(request).criteriaVersion(version).build());
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> entityManager.createNativeQuery(
                        "update analysis_runs set status = :status where id = :id")
                .setParameter("status", "COMPLETEDD")
                .setParameter("id", run.getId())
                .executeUpdate()).isInstanceOf(PersistenceException.class);
    }

    @Test
    void rejectsDuplicateKakaoId() {
        persist(User.builder().kakaoId(100L).build());
        assertThatThrownBy(() -> {
            persist(User.builder().kakaoId(100L).build());
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void rejectsDuplicateVersionNumberWithinProject() {
        User user = persist(User.builder().kakaoId(100L).build());
        Project project = persist(Project.builder().owner(user).name("프로젝트").build());
        persist(CriteriaVersion.builder().project(project).versionNo(1).build());
        assertThatThrownBy(() -> {
            persist(CriteriaVersion.builder().project(project).versionNo(1).build());
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void rejectsDuplicateItemOrderWithinAnalysis() {
        User user = persist(User.builder().kakaoId(100L).build());
        Project project = persist(Project.builder().owner(user).name("프로젝트").build());
        CriteriaVersion version = persist(CriteriaVersion.builder().project(project).versionNo(1).build());
        ClientRequest request = persist(ClientRequest.builder().project(project).requestText("요청").build());
        AnalysisRun run = persist(AnalysisRun.builder().clientRequest(request).criteriaVersion(version).build());
        persist(AnalysisItem.builder().analysisRun(run).itemOrder(1).requestExcerpt("요청")
                .classification(AnalysisClassification.IN_SCOPE).reason("기준 내").build());
        assertThatThrownBy(() -> {
            persist(AnalysisItem.builder().analysisRun(run).itemOrder(1).requestExcerpt("요청")
                    .classification(AnalysisClassification.OUT_OF_SCOPE).reason("기준 외").build());
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void preventsDeletingReferencedUser() {
        User user = persist(User.builder().kakaoId(100L).build());
        persist(Project.builder().owner(user).name("프로젝트").build());
        entityManager.flush();
        entityManager.clear();
        User referencedUser = entityManager.find(User.class, user.getId());
        assertThatThrownBy(() -> {
            entityManager.remove(referencedUser);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
}
