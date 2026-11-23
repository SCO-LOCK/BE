package com.scolock.BE_SCOLOCK.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:halfthon-seed;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE",
        "spring.sql.init.mode=always",
        "spring.sql.init.encoding=UTF-8",
        "spring.jpa.defer-datasource-initialization=true"
})
@ActiveProfiles("test")
class HalfthonSeedTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    @Test
    void initializesLinkedFixturesAndPreservesDataOnRestart() {
        assertThat(jdbc.queryForObject("SELECT owner_id FROM projects WHERE id = 1", Long.class))
                .isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT criteria_text FROM criteria_versions WHERE project_id = 1 AND version_no = 1", String.class))
                .contains("[영상]", "15초", "9:16", "Instagram Reels 광고", "[스토리 / 구성]",
                        "[분위기 / 스타일]", "[인물]", "검은 정장", "[제품 / 브랜드]",
                        "[카메라 / 연출]", "[레퍼런스]", "[원고]", "현재 확정된 원고 사용");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM client_requests WHERE project_id = 1", Integer.class))
                .isEqualTo(8);
        assertThat(jdbc.queryForList("SELECT request_text FROM client_requests WHERE id IN (2, 3)", String.class))
                .containsExactly("전체적으로 조금 더 트렌디하게 해주세요.", "전체적으로 조금 더 트렌디하게 해주세요.");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM analysis_runs", Integer.class)).isZero();

        jdbc.update("UPDATE projects SET name = '테스트 중 수정한 프로젝트 이름' WHERE id = 1");
        var populator = new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM projects", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM criteria_versions", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM client_requests", Integer.class)).isEqualTo(8);
        assertThat(jdbc.queryForObject("SELECT name FROM projects WHERE id = 1", String.class))
                .isEqualTo("테스트 중 수정한 프로젝트 이름");

        jdbc.update("INSERT INTO client_requests (project_id, request_text, created_at, updated_at) VALUES (1, '추가 요청', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        assertThat(jdbc.queryForObject("SELECT id FROM client_requests WHERE request_text = '추가 요청'", Long.class))
                .isGreaterThan(8L);
    }
}
