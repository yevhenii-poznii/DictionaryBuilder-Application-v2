package com.kiskee.vocabulary.web.controller.repetition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.service.vocabulary.repetition.InputRepetitionService;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InputRepetitionController.class)
public class InputRepetitionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private InputRepetitionService inputRepetitionService;

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

        when(inputRepetitionService.isRepetitionRunning()).thenReturn(runningStatus);

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

        when(inputRepetitionService.isRepetitionRunning()).thenReturn(runningStatus);

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

        when(inputRepetitionService.isRepetitionRunning()).thenReturn(runningStatus);

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

        when(inputRepetitionService.isRepetitionRunning())
                .thenThrow(new ResourceNotFoundException(String.format("Repetition data not found for user [%s]", "userId")));

        mockMvc.perform(get("/repetition/running"))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage")
                                .value("Repetition data not found for user [userId]"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testStart_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false);

        when(inputRepetitionService.start(dictionaryId, requestBody)).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(post("/repetition/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }
}
