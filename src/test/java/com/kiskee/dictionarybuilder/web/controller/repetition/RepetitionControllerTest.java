package com.kiskee.dictionarybuilder.web.controller.repetition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionServiceFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.RepetitionHandler;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Disabled
@ExtendWith(SpringExtension.class)
@WebMvcTest(RepetitionController.class)
public class RepetitionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private RepetitionServiceFactory repetitionServiceFactory;

    @MockBean
    private RepetitionHandler defaultRepetitionHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    //    @Test
    //    @SneakyThrows
    //    void testIsRepetitionRunning_WhenRepetitionIsRunningAndNotPaused_ThenReturnRepetitionRunningStatus() {
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false);
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(get("/repetition/running"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testIsRepetitionRunning_WhenRepetitionIsRunningAndPaused_ThenReturnRepetitionRunningStatus() {
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, true);
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(get("/repetition/running"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testIsRepetitionRunning_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus() {
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(false, false);
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(get("/repetition/running"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testIsRepetitionRunning_WhenRepetitionDataIsNull_ThenReturnNotFound() {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.isRepetitionRunning())
    //                .thenThrow(new ResourceNotFoundException(
    //                        String.format("Repetition data not found for user [%s]", "userId")));
    //
    //        mockMvc.perform(get("/repetition/running"))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isNotFound(),
    //                        jsonPath("$.errors.responseMessage").value("Repetition data not found for user
    // [userId]"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testPause_WhenRepetitionIsRunningAndNotPaused_ThenReturnRepetitionRunningStatus() {
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, true);
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.pause()).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(put("/repetition/pause"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testPause_WhenRepetitionIsNotRunning_ThenReturnBadRequest() {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.pause()).thenThrow(new RepetitionException("Repetition is not running"));
    //
    //        mockMvc.perform(put("/repetition/pause"))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("Repetition is not running"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testPause_WhenRepetitionIsPaused_ThenReturnBadRequest() {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.pause()).thenThrow(new RepetitionException("Pause already started"));
    //
    //        mockMvc.perform(put("/repetition/pause"))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("Pause already started"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testUnpause_WhenRepetitionIsRunningAndPaused_ThenReturnRepetitionRunningStatus() {
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false);
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.unpause()).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(put("/repetition/unpause"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testUnpause_WhenRepetitionIsNotRunning_ThenReturnBadRequest() {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.unpause()).thenThrow(new RepetitionException("Repetition is not running"));
    //
    //        mockMvc.perform(put("/repetition/unpause"))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("Repetition is not running"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testUnpause_WhenThereAreNoStartedPauses_ThenReturnBadRequest() {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.unpause()).thenThrow(new RepetitionException("No pause to end"));
    //
    //        mockMvc.perform(put("/repetition/unpause"))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("No pause to end"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testStop_WhenRepetitionIsRunning_ThenReturnRepetitionRunningStatus() {
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(false, false);
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.stop()).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(delete("/repetition"))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @Test
    //    @SneakyThrows
    //    void testStop_WhenRepetitionIsNotRunning_ThenReturnBadRequest() {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        when(repetitionServiceFactory.execute(any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(0))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.stop()).thenThrow(new RepetitionException("Repetition is not running"));
    //
    //        mockMvc.perform(delete("/repetition"))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("Repetition is not running"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @SneakyThrows
    //    @ParameterizedTest
    //    @MethodSource("provideRepetitionTypes")
    //    void testStart_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus(RepetitionType repetitionType) {
    //        long dictionaryId = 1L;
    //        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
    //                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
    //                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
    //                repetitionType,
    //                false);
    //
    //        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false, repetitionType);
    //
    //        when(repetitionServiceFactory.execute(any(), any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(1))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.start(dictionaryId, requestBody)).thenReturn(runningStatus);
    //
    //        MvcResult result = mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .content(objectMapper.writeValueAsString(requestBody)))
    //                .andDo(print())
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        String actualResponseBody = result.getResponse().getContentAsString();
    //        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    //    }
    //
    //    @SneakyThrows
    //    @ParameterizedTest
    //    @MethodSource("provideRepetitionTypes")
    //    void testStart_WhenRepetitionIsAlreadyRunning_ThenReturnBadRequest(RepetitionType repetitionType) {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        long dictionaryId = 1L;
    //        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
    //                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
    //                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
    //                repetitionType,
    //                false);
    //
    //        when(repetitionServiceFactory.execute(any(), any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(1))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.start(dictionaryId, requestBody))
    //                .thenThrow(new RepetitionException("Repetition is already running"));
    //
    //        mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .param("repetitionType", repetitionType.name())
    //                        .content(objectMapper.writeValueAsString(requestBody)))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("Repetition is already running"));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @SneakyThrows
    //    @ParameterizedTest
    //    @MethodSource("provideRepetitionTypes")
    //    void testStart_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound(RepetitionType repetitionType) {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        long dictionaryId = 1L;
    //        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
    //                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
    //                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
    //                repetitionType,
    //                false);
    //
    //        String exceptionMessage = String.format(
    //                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(),
    // dictionaryId);
    //        when(repetitionServiceFactory.execute(any(), any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(1))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.start(dictionaryId, requestBody))
    //                .thenThrow(new ResourceNotFoundException(exceptionMessage));
    //
    //        mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .param("repetitionType", repetitionType.name())
    //                        .content(objectMapper.writeValueAsString(requestBody)))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isNotFound(),
    //                        jsonPath("$.errors.responseMessage").value(exceptionMessage));
    //
    //        TimeZoneContextHolder.clear();
    //    }
    //
    //    @SneakyThrows
    //    @ParameterizedTest
    //    @MethodSource("provideRepetitionTypes")
    //    void testStart_WhenNoWordsForRepetition_ThenReturnBadRequest(RepetitionType repetitionType) {
    //        TimeZoneContextHolder.setTimeZone("UTC");
    //
    //        long dictionaryId = 1L;
    //        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
    //                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
    //                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
    //                repetitionType,
    //                false);
    //
    //        when(repetitionServiceFactory.execute(any(), any()))
    //                .thenAnswer(
    //                        invocation -> ((Function<RepetitionService, RepetitionRunningStatus>)
    // invocation.getArgument(1))
    //                                .apply(defaultRepetitionHandler));
    //
    //        when(defaultRepetitionHandler.start(dictionaryId, requestBody))
    //                .thenThrow(new RepetitionException("No words to repeat"));
    //
    //        mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .param("repetitionType", repetitionType.name())
    //                        .content(objectMapper.writeValueAsString(requestBody)))
    //                .andDo(print())
    //                .andExpectAll(
    //                        status().isBadRequest(),
    //                        jsonPath("$.errors.responseMessage").value("No words to repeat"));
    //
    //        TimeZoneContextHolder.clear();
    //    }

    private static Stream<RepetitionType> provideRepetitionTypes() {
        return Stream.of(RepetitionType.values());
    }
}
