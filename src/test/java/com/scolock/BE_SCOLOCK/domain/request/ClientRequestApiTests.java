package com.scolock.BE_SCOLOCK.domain.request;

import com.jayway.jsonpath.JsonPath;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import com.scolock.BE_SCOLOCK.domain.request.repository.ClientRequestRepository;
import com.scolock.BE_SCOLOCK.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClientRequestApiTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ClientRequestRepository clientRequestRepository;

    private Long projectId;

    @BeforeEach
    void setUp() {
        // 자동 증가 시퀀스나 테스트 실행 순서에 의존하지 않고 임시 사용자 ID를 준비한다.
        jdbcTemplate.update("""
                insert into users (id, kakao_id, created_at, updated_at)
                values (1, 1001, current_timestamp, current_timestamp),
                       (2, 1002, current_timestamp, current_timestamp)
                """);
        projectId = createProject(1L);
    }

    @Test
    void createsRequestAndPersistsProjectRelationshipAndCreatedAt() throws Exception {
        String response = mockMvc.perform(post("/projects/{projectId}/requests", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestText\":\"  버튼 색상을 변경해 주세요.\\n다음 주까지 부탁드립니다.  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.requestText").value("  버튼 색상을 변경해 주세요.\n다음 주까지 부탁드립니다.  "))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        Number id = JsonPath.read(response, "$.data.id");
        String createdAt = JsonPath.read(response, "$.data.createdAt");
        entityManager.flush();
        entityManager.clear();
        ClientRequest saved = clientRequestRepository.findById(id.longValue()).orElseThrow();
        assertThat(saved.getProject().getId()).isEqualTo(projectId);
        assertThat(saved.getProject().getOwner().getId()).isEqualTo(1L);
        assertThat(saved.getRequestText()).isEqualTo("  버튼 색상을 변경해 주세요.\n다음 주까지 부탁드립니다.  ");
        // DB의 타임스탬프 정밀도 차이를 허용한다.
        assertThat(saved.getCreatedAt()).isCloseTo(LocalDateTime.parse(createdAt),
                org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MILLIS));
        assertThat(clientRequestRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"requestText\":null}", "{\"requestText\":\"\"}",
            "{\"requestText\":\" \\t\\n \"}"})
    void rejectsMissingOrBlankText(String body) throws Exception {
        mockMvc.perform(post("/projects/{projectId}/requests", projectId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"))
                .andExpect(jsonPath("$.data.requestText").isNotEmpty());
        assertThat(clientRequestRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{", "{\"requestText\":{}}", "{\"requestText\":[]}"})
    void rejectsInvalidJsonOrMissingBody(String body) throws Exception {
        mockMvc.perform(post("/projects/{projectId}/requests", projectId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
        assertThat(clientRequestRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "9223372036854775808"})
    void rejectsInvalidProjectId(String id) throws Exception {
        mockMvc.perform(post("/projects/{projectId}/requests", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"requestText\":\"수정 요청\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400"));
        assertThat(clientRequestRepository.count()).isZero();
    }

    @Test
    void rejectsNonexistentProject() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/requests", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"requestText\":\"수정 요청\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT404"));
        assertThat(clientRequestRepository.count()).isZero();
    }

    @Test
    void rejectsAnotherUsersProject() throws Exception {
        Long otherProjectId = createProject(2L);
        mockMvc.perform(post("/projects/{projectId}/requests", otherProjectId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"requestText\":\"수정 요청\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON403"));
        assertThat(clientRequestRepository.count()).isZero();
    }

    private Long createProject(Long ownerId) {
        Project project = Project.builder().owner(entityManager.getReference(User.class, ownerId))
                .name("테스트 프로젝트").build();
        entityManager.persist(project);
        entityManager.flush();
        return project.getId();
    }
}
