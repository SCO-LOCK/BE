package com.scolock.BE_SCOLOCK.domain.analysis.ai;

import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisClassification;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestAnalysisClientTests {
    private final ChatModel model = mock(ChatModel.class);
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final RequestAnalysisClient client = new RequestAnalysisClient(model, JsonMapper.builder().build(),
            factory.getValidator(), "test-model", Duration.ofSeconds(60));

    @AfterEach
    void close() { factory.close(); }

    @Test
    void sendsBaselineAndOriginalRequestSeparatelyFromInstructions() {
        respond("{\"findings\":[]}", "stop");
        assertThat(client.analyze("파란색", "  붉은색으로 변경\n").findings()).isEmpty();
        var captor = ArgumentCaptor.forClass(Prompt.class);
        verify(model).call(captor.capture());
        Prompt prompt = captor.getValue();
        assertThat(prompt.getInstructions()).hasSize(2);
        var input = JsonMapper.builder().build().readTree(prompt.getInstructions().get(1).getText());
        assertThat(input.get("existing_baseline").asText()).isEqualTo("파란색");
        assertThat(input.get("new_request").asText()).isEqualTo("  붉은색으로 변경\n");
        assertThat(((OpenAiChatOptions) prompt.getOptions()).getOutputSchema()).contains("MISSING_INFO", "request_excerpt");
        var mapper = JsonMapper.builder().build();
        var schema = mapper.readTree(((OpenAiChatOptions) prompt.getOptions()).getOutputSchema());
        assertThat(mapper.treeToValue(schema.get("required"), String[].class)).containsExactly("findings");
        var findingSchema = schema.get("properties").get("findings").get("items");
        assertThat(mapper.treeToValue(findingSchema.get("properties").get("type").get("enum"), String[].class))
                .containsExactlyInAnyOrder("MISSING_INFO", "AMBIGUOUS", "BASELINE_CHANGE", "TECH_CHECK");
        assertThat(mapper.treeToValue(findingSchema.get("required"), String[].class))
                .containsExactlyInAnyOrder("type", "title", "existing_baseline", "request_excerpt",
                        "reason", "suggested_question");
        assertThat(schema.get("additionalProperties").asBoolean()).isFalse();
        assertThat(findingSchema.get("additionalProperties").asBoolean()).isFalse();
    }

    @Test
    void acceptsMissingInformationAsFinding() {
        respond("""
                {"findings":[{"type":"MISSING_INFO","title":"색상 확인","existing_baseline":"",
                "request_excerpt":"색상 변경","reason":"색상 값 누락","suggested_question":"어떤 색상인가요?"}]}
                """, "stop");
        assertThat(client.analyze(null, "색상 변경").findings()).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(AnalysisClassification.class)
    void acceptsEveryClassification(AnalysisClassification classification) {
        respond("""
                {"findings":[{"type":"%s","title":"확인 필요","existing_baseline":"기준",
                "request_excerpt":"요청","reason":"확인이 필요함","suggested_question":"확인해 주시겠어요?"}]}
                """.formatted(classification.name()), "stop");
        assertThat(client.analyze("기준", "요청").findings())
                .singleElement().satisfies(finding -> assertThat(finding.type()).isEqualTo(classification));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not json", "{}", "{\"findings\":null}", "{\"findings\":[null]}",
            "{\"findings\":[{\"type\":\"IN_SCOPE\"}]}",
            """
            {"findings":[{"type":"AMBIGUOUS","title":"제목","existing_baseline":"",
            "request_excerpt":"없는 원문","reason":"이유","suggested_question":"질문"}]}
            """,
            """
            {"findings":[{"type":"AMBIGUOUS","title":"","existing_baseline":"",
            "request_excerpt":"요청","reason":"이유","suggested_question":"질문"}]}
            """})
    void rejectsMalformedOrUngroundedResponses(String json) {
        respond(json, "stop");
        assertThatThrownBy(() -> client.analyze("기준", "요청"))
                .isInstanceOfSatisfying(AnalysisAiException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AI_INVALID_RESPONSE"));
    }

    @Test
    void rejectsTruncatedResponse() {
        respond("{\"findings\":[]}", "length");
        assertThatThrownBy(() -> client.analyze("기준", "요청"))
                .isInstanceOfSatisfying(AnalysisAiException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AI_INVALID_RESPONSE"));
    }

    @Test
    void distinguishesTimeoutAndApiErrorWithoutExposingProviderMessage() {
        when(model.call(any(Prompt.class))).thenThrow(new RuntimeException("secret provider details"));
        assertThatThrownBy(() -> client.analyze("기준", "요청"))
                .isInstanceOfSatisfying(AnalysisAiException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("AI_API_ERROR");
                    assertThat(e.getMessage()).doesNotContain("secret");
                });
        reset(model);
        when(model.call(any(Prompt.class))).thenThrow(new RuntimeException(new java.net.SocketTimeoutException()));
        assertThatThrownBy(() -> client.analyze("기준", "요청"))
                .isInstanceOfSatisfying(AnalysisAiException.class,
                        e -> assertThat(e.getCode()).isEqualTo("AI_TIMEOUT"));
    }

    private void respond(String text, String finishReason) {
        when(model.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(
                new AssistantMessage(text), ChatGenerationMetadata.builder().finishReason(finishReason).build()))));
    }
}
