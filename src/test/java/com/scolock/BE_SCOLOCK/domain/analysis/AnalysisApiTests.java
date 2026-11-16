package com.scolock.BE_SCOLOCK.domain.analysis;

import com.scolock.BE_SCOLOCK.domain.analysis.ai.RequestAnalysisClient;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisWorker;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisPersistenceService;
import org.springframework.core.task.TaskRejectedException;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResult;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import com.jayway.jsonpath.JsonPath;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisClassification;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisRunRepository;
import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import com.scolock.BE_SCOLOCK.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalysisApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AnalysisRunRepository analysisRunRepository;

    @MockitoBean private RequestAnalysisClient aiClient;
    @MockitoBean private AnalysisWorker analysisWorker;
    @Autowired private AnalysisPersistenceService persistence;

    private Project project;
    private ClientRequest request;
    private CriteriaVersion criteria;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
                insert into users (id, kakao_id, created_at, updated_at)
                values (1, 1001, current_timestamp, current_timestamp),
                       (2, 1002, current_timestamp, current_timestamp)
                """);
        when(aiClient.analyze(any(), anyString())).thenReturn(new AnalysisResult(java.util.List.of()));
        when(aiClient.modelName()).thenReturn("test-model");
        project = createProject(1L);
        request = ClientRequest.builder().project(project).requestText("버튼 색상 변경").build();
        criteria = CriteriaVersion.builder().project(project).versionNo(1).criteriaText("제작 기준").build();
        entityManager.persist(request);
        entityManager.persist(criteria);
        entityManager.flush();
    }

    @Test
    void acceptsAnalysisWithoutWaitingForAi() throws Exception {
        String response = submit(project.getId(), request.getId())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value("COMMON202"))
                .andExpect(jsonPath("$.data.analysisId").isNumber())
                .andExpect(jsonPath("$.data.status").doesNotHaveJsonPath())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.data.analysisId");
        entityManager.flush();
        entityManager.clear();
        AnalysisRun saved = analysisRunRepository.findById(id.longValue()).orElseThrow();
        assertThat(saved.getClientRequest().getId()).isEqualTo(request.getId());
        assertThat(saved.getCriteriaVersion().getId()).isEqualTo(criteria.getId());
        assertThat(saved.getStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getResultJson()).isNull();
        verify(analysisWorker).analyze(new AnalysisPersistenceService.AnalysisInput(
                id.longValue(), "제작 기준", "버튼 색상 변경"));
        verifyNoInteractions(aiClient);
        assertThat(saved.getStartedAt()).isNull();
        assertThat(saved.getFinishedAt()).isNull();
        assertThat(analysisRunRepository.count()).isEqualTo(1);
    }

    @Test
    void recordsProcessingAndStartTimeWhenWorkerStarts() {
        var input = persistence.start(project.getId(), request.getId(), 1L);
        entityManager.flush();
        entityManager.clear();
        AnalysisRun pending = analysisRunRepository.findById(input.analysisId()).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(pending.getStartedAt()).isNull();

        persistence.markProcessing(input.analysisId());
        entityManager.flush();
        entityManager.clear();

        AnalysisRun processing = analysisRunRepository.findById(input.analysisId()).orElseThrow();
        assertThat(processing.getStatus()).isEqualTo(AnalysisStatus.PROCESSING);
        assertThat(processing.getStartedAt()).isNotNull();
        assertThat(processing.getFinishedAt()).isNull();
    }

    @Test
    void selectsHighestVersionWithinProjectRegardlessOfInsertionOrder() throws Exception {
        CriteriaVersion latest = CriteriaVersion.builder().project(project).versionNo(3).criteriaText("최신 기준").build();
        CriteriaVersion older = CriteriaVersion.builder().project(project).versionNo(2).criteriaText("이전 기준").build();
        CriteriaVersion other = CriteriaVersion.builder().project(createProject(1L)).versionNo(10).build();
        entityManager.persist(latest);
        entityManager.persist(older);
        entityManager.persist(other);
        entityManager.flush();

        String response = submit(project.getId(), request.getId())
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.data.analysisId");
        entityManager.flush();
        entityManager.clear();
        AnalysisRun saved = analysisRunRepository.findById(id.longValue()).orElseThrow();
        assertThat(saved.getCriteriaVersion().getId()).isEqualTo(latest.getId());
    }

    @Test
    void rejectsMissingCriteriaEvenWhenAnotherProjectHasCriteria() throws Exception {
        entityManager.remove(criteria);
        CriteriaVersion other = CriteriaVersion.builder().project(createProject(1L)).versionNo(1).build();
        entityManager.persist(other);
        entityManager.flush();

        submit(project.getId(), request.getId())
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("CRITERIA404"));
        assertThat(analysisRunRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "9223372036854775808"})
    void rejectsInvalidPathIds(String id) throws Exception {
        submit(id, request.getId()).andExpect(status().isBadRequest());
        submit(project.getId(), id).andExpect(status().isBadRequest());
        assertThat(analysisRunRepository.count()).isZero();
    }

    @Test
    void rejectsMissingResourcesWithoutSaving() throws Exception {
        submit(Long.MAX_VALUE, request.getId())
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PROJECT404"));
        submit(project.getId(), Long.MAX_VALUE)
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("REQUEST404"));
        assertThat(analysisRunRepository.count()).isZero();
    }

    @Test
    void rejectsResourcesFromAnotherProjectWithoutSaving() throws Exception {
        Project other = createProject(1L);
        ClientRequest otherRequest = ClientRequest.builder().project(other).requestText("다른 요청").build();
        entityManager.persist(otherRequest);
        entityManager.flush();
        submit(project.getId(), otherRequest.getId())
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("REQUEST404"));
        assertThat(analysisRunRepository.count()).isZero();
    }

    @Test
    void rejectsAnotherUsersProjectWithoutSaving() throws Exception {
        Project other = createProject(2L);
        submit(other.getId(), request.getId())
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("COMMON403"));
        assertThat(analysisRunRepository.count()).isZero();
    }

    @Test
    void failurePreservesRequestAndAllowsRetry() throws Exception {
        String failureBody = submit(project.getId(), request.getId()).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data.errorCode").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.data.errorMessage").doesNotHaveJsonPath())
                .andReturn().getResponse().getContentAsString();
        Number failedId = JsonPath.read(failureBody, "$.data.analysisId");
        persistence.markProcessing(failedId.longValue());
        persistence.fail(failedId.longValue(), "AI_TIMEOUT", "시간 초과");
        AnalysisRun failed = analysisRunRepository.findById(failedId.longValue()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(failed.getErrorCode()).isEqualTo("AI_TIMEOUT");
        assertThat(failed.getErrorMessage()).isEqualTo("시간 초과");
        assertThat(request.getRequestText()).isEqualTo("버튼 색상 변경");
        submit(project.getId(), request.getId()).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").doesNotHaveJsonPath());
        assertThat(analysisRunRepository.count()).isEqualTo(2);
    }

    @Test
    void persistsFindingsAndReturnsOnlyId() throws Exception {
        var finding = new AnalysisResult.Finding(AnalysisClassification.MISSING_INFO,
                "색상 확인", "제작 기준", "버튼 색상 변경", "색상 값이 없습니다.", "어떤 색상으로 변경할까요?");

        String body = submit(project.getId(), request.getId()).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.analysisId").isNumber())
                .andExpect(jsonPath("$.data.status").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data.findings").doesNotHaveJsonPath())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.data.analysisId");
        persistence.markProcessing(id.longValue());
        persistence.complete(id.longValue(), new AnalysisResult(java.util.List.of(finding)), "test-model");
        entityManager.flush();
        entityManager.clear();
        AnalysisRun saved = analysisRunRepository.findById(id.longValue()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        var result = JsonPath.parse(saved.getResultJson());
        assertThat(result.<String>read("$.findings[0].type")).isEqualTo("MISSING_INFO");
        assertThat(result.<String>read("$.findings[0].suggested_question"))
                .isEqualTo("어떤 색상으로 변경할까요?");
    }

    @Test
    void rejectsBlankStoredRequestWithoutCallingAi() throws Exception {
        ClientRequest blank = ClientRequest.builder().project(project).requestText("   ").build();
        entityManager.persist(blank);
        submit(project.getId(), blank.getId()).andExpect(status().isBadRequest());
        verify(aiClient, never()).analyze(any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = AnalysisStatus.class, names = {"PENDING", "PROCESSING"})
    void rejectsDuplicateInProgressAnalysis(AnalysisStatus analysisStatus) throws Exception {
        analysisRunRepository.save(AnalysisRun.builder().clientRequest(request).criteriaVersion(criteria)
                .status(analysisStatus).build());
        submit(project.getId(), request.getId()).andExpect(status().isConflict());
        verify(aiClient, never()).analyze(any(), any());
    }

    @Test
    void marksRejectedTaskFailedAndAllowsRetry() throws Exception {
        doThrow(new TaskRejectedException("full")).doNothing().when(analysisWorker).analyze(any());
        submit(project.getId(), request.getId()).andExpect(status().isServiceUnavailable());
        AnalysisRun failed = analysisRunRepository.findAll().getFirst();
        assertThat(failed.getStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(failed.getErrorCode()).isEqualTo("ANALYSIS_UNAVAILABLE");
        assertThat(failed.getStartedAt()).isNull();
        assertThat(failed.getFinishedAt()).isNotNull();
        submit(project.getId(), request.getId()).andExpect(status().isAccepted());
    }

    private ResultActions submit(Object projectId, Object requestId) throws Exception {
        return mockMvc.perform(post("/projects/{projectId}/requests/{requestId}/analyses", projectId, requestId));
    }

    private Project createProject(Long ownerId) {
        Project result = Project.builder().owner(entityManager.getReference(User.class, ownerId))
                .name("테스트 프로젝트").build();
        entityManager.persist(result);
        entityManager.flush();
        return result;
    }
}
