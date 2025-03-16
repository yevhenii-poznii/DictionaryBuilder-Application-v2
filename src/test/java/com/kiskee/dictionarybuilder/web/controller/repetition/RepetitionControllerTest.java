package com.kiskee.dictionarybuilder.web.controller.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionServiceFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.RepetitionHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.RepetitionStateHandler;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RepetitionController.class)
public class RepetitionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private RepetitionServiceFactory RepetitionHandlerFactory;

    @MockBean
    private RepetitionStateHandler defaultRepetitionHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testIsRepetitionRunning_WhenRepetitionIsRunningAndNotPaused_ThenReturnRepetitionRunningStatus() {
        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false);

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(get("/repetition/running"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testIsRepetitionRunning_WhenRepetitionIsRunningAndPaused_ThenReturnRepetitionRunningStatus() {
        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, true);

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(get("/repetition/running"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testIsRepetitionRunning_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus() {
        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(false, false);

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(get("/repetition/running"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testIsRepetitionRunning_WhenRepetitionDataIsNull_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("UTC");

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.isRepetitionRunning())
                .thenThrow(new ResourceNotFoundException(
                        String.format("Repetition data not found for user [%s]", "userId")));

        mockMvc.perform(get("/repetition/running"))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Repetition data not found for user [userId]"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testPause_WhenRepetitionIsRunningAndNotPaused_ThenReturnRepetitionRunningStatus() {
        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, true);

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.pause()).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(put("/repetition/pause"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testPause_WhenRepetitionIsNotRunning_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.pause()).thenThrow(new RepetitionException("Repetition is not running"));

        mockMvc.perform(put("/repetition/pause"))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("Repetition is not running"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testPause_WhenRepetitionIsPaused_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.pause()).thenThrow(new RepetitionException("Pause already started"));

        mockMvc.perform(put("/repetition/pause"))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("Pause already started"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUnpause_WhenRepetitionIsRunningAndPaused_ThenReturnRepetitionRunningStatus() {
        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false);

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.unpause()).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(put("/repetition/unpause"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testUnpause_WhenRepetitionIsNotRunning_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.unpause()).thenThrow(new RepetitionException("Repetition is not running"));

        mockMvc.perform(put("/repetition/unpause"))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("Repetition is not running"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUnpause_WhenThereAreNoStartedPauses_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.unpause()).thenThrow(new RepetitionException("No pause to end"));

        mockMvc.perform(put("/repetition/unpause"))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("No pause to end"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testStop_WhenRepetitionIsRunning_ThenReturnRepetitionRunningStatus() {
        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(false, false);

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.stop()).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(delete("/repetition"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testStop_WhenRepetitionIsNotRunning_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        when(RepetitionHandlerFactory.execute(any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(0))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.stop()).thenThrow(new RepetitionException("Repetition is not running"));

        mockMvc.perform(delete("/repetition"))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("Repetition is not running"));

        TimeZoneContextHolder.clear();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus(RepetitionType repetitionType) {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false, repetitionType);

        when(RepetitionHandlerFactory.execute(any(), any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(1))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.start(dictionaryId, requestBody)).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenRepetitionIsAlreadyRunning_ThenReturnBadRequest(RepetitionType repetitionType) {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        when(RepetitionHandlerFactory.execute(any(), any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(1))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.start(dictionaryId, requestBody))
                .thenThrow(new RepetitionException("Repetition is already running"));

        mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("repetitionType", repetitionType.name())
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("Repetition is already running"));

        TimeZoneContextHolder.clear();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound(RepetitionType repetitionType) {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        String exceptionMessage = String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId);
        when(RepetitionHandlerFactory.execute(any(), any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(1))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.start(dictionaryId, requestBody))
                .thenThrow(new ResourceNotFoundException(exceptionMessage));

        mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("repetitionType", repetitionType.name())
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value(exceptionMessage));

        TimeZoneContextHolder.clear();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenNoWordsForRepetition_ThenReturnBadRequest(RepetitionType repetitionType) {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        when(RepetitionHandlerFactory.execute(any(), any()))
                .thenAnswer(
                        invocation -> ((Function<RepetitionHandler, RepetitionRunningStatus>) invocation.getArgument(1))
                                .apply(defaultRepetitionHandler));

        when(defaultRepetitionHandler.start(dictionaryId, requestBody))
                .thenThrow(new RepetitionException("No words to repeat"));

        mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("repetitionType", repetitionType.name())
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("No words to repeat"));

        TimeZoneContextHolder.clear();
    }

    private static Stream<RepetitionType> provideRepetitionTypes() {
        return Stream.of(RepetitionType.values());
    }
}
