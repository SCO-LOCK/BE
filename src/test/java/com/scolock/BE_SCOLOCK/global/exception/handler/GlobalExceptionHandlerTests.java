package com.scolock.BE_SCOLOCK.global.exception.handler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

    private MockMvc mockMvc;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @ParameterizedTest
    @CsvSource({
            "unexpected, 500, COMMON500",
            "application, 500, COMMON500",
            "unavailable, 503, COMMON503",
            "mvc, 500, COMMON500",
            "timeout, 503, COMMON503"
    })
    void serverErrorReturnsCommonResponseAndLogsException(String kind, int httpStatus, String code) throws Exception {
        String body = mockMvc.perform(get("/test/errors/{kind}", kind))
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").value(httpStatus == 503
                        ? "서비스를 일시적으로 이용할 수 없습니다." : "서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
        assertThat(body).doesNotContain("internal-detail", "stackTrace", "java.lang");
    }

    @ParameterizedTest
    @CsvSource({"/test/errors/missing, 404, PROJECT404", "/test/numbers/abc, 400, COMMON400"})
    void clientErrorReturnsCommonResponseWithoutErrorLog(String path, int httpStatus, String code) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data").isEmpty());
        assertThat(appender.list).isEmpty();
    }

    @Test
    void successReturnsCommonResponseWithoutErrorLog() throws Exception {
        mockMvc.perform(get("/test/numbers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."))
                .andExpect(jsonPath("$.data").value(1));
        assertThat(appender.list).isEmpty();
    }

    @RestController
    static class ErrorTestController {

        @GetMapping("/test/errors/{kind}")
        public ApiResponse<Void> error(@PathVariable String kind) {
            throw switch (kind) {
                case "application" -> new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
                case "unavailable" -> new GeneralException(ErrorStatus.SERVICE_UNAVAILABLE);
                case "mvc" -> new HttpMessageNotWritableException("internal-detail");
                case "timeout" -> new AsyncRequestTimeoutException();
                case "missing" -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND);
                default -> new IllegalStateException("internal-detail");
            };
        }

        @GetMapping("/test/numbers/{number}")
        public ApiResponse<Integer> number(@PathVariable int number) {
            return ApiResponse.onSuccess(number);
        }
    }
}
