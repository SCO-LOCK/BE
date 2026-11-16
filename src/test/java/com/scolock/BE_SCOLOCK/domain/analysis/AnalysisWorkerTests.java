package com.scolock.BE_SCOLOCK.domain.analysis;

import com.scolock.BE_SCOLOCK.domain.analysis.ai.AnalysisAiException;
import com.scolock.BE_SCOLOCK.domain.analysis.ai.RequestAnalysisClient;
import com.scolock.BE_SCOLOCK.domain.analysis.config.AnalysisAsyncConfig;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResult;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisItemRepository;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisRunRepository;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisPersistenceService;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisPersistenceService.AnalysisInput;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisService;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisWorker;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {AnalysisAsyncConfig.class, AnalysisService.class, AnalysisWorker.class})
class AnalysisWorkerTests {

    @Autowired private AnalysisService service;
    @MockitoBean private AnalysisPersistenceService persistence;
    @MockitoBean private RequestAnalysisClient aiClient;
    @MockitoBean private AnalysisRunRepository analysisRunRepository;
    @MockitoBean private AnalysisItemRepository analysisItemRepository;

    @Test
    void respondsWhileAiIsStillRunningThenSavesResult() throws Exception {
        AnalysisInput input = new AnalysisInput(10L, "기준", "요청");
        AnalysisResult result = new AnalysisResult(List.of());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(persistence.start(1L, 2L, 3L)).thenReturn(input);
        when(aiClient.modelName()).thenReturn("test-model");
        when(aiClient.analyze("기준", "요청")).thenAnswer(invocation -> {
            started.countDown();
            assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
            return result;
        });

        try {
            var response = service.create(1L, 2L, 3L);
            assertThat(response.analysisId()).isEqualTo(10L);
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            var order = inOrder(persistence, aiClient);
            order.verify(persistence).markProcessing(10L);
            order.verify(aiClient).analyze("기준", "요청");
            verify(persistence, never()).complete(anyLong(), any(), any());
        } finally {
            release.countDown();
        }
        verify(persistence, timeout(5000)).complete(10L, result, "test-model");
    }

    @Test
    void savesAiFailureAfterAcceptance() {
        when(persistence.start(1L, 2L, 3L)).thenReturn(new AnalysisInput(10L, "기준", "요청"));
        when(aiClient.analyze("기준", "요청"))
                .thenThrow(new AnalysisAiException("AI_TIMEOUT", "시간 초과", null));

        assertThat(service.create(1L, 2L, 3L).analysisId()).isEqualTo(10L);

        verify(persistence, timeout(5000)).fail(10L, "AI_TIMEOUT", "시간 초과");
        verify(persistence, never()).complete(anyLong(), any(), any());
    }

    @Test
    void marksUnexpectedFailureInsteadOfLeavingAnalysisProcessing() {
        when(persistence.start(1L, 2L, 3L)).thenReturn(new AnalysisInput(10L, "기준", "요청"));
        when(aiClient.analyze("기준", "요청")).thenThrow(new IllegalStateException("unexpected"));

        service.create(1L, 2L, 3L);

        verify(persistence, timeout(5000)).fail(10L, "ANALYSIS_ERROR", "분석 처리 중 오류가 발생했습니다.");
    }
}
