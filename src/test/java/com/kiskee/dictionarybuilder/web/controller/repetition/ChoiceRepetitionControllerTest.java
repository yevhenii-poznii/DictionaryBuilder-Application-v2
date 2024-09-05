package com.kiskee.dictionarybuilder.web.controller.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.choice.ChoiceRepetitionService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChoiceRepetitionController.class)
public class ChoiceRepetitionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private ChoiceRepetitionService choiceRepetitionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testStart_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus() {
        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        RepetitionRunningStatus runningStatus = new RepetitionRunningStatus(true, false, RepetitionType.CHOICE);

        when(choiceRepetitionService.start(dictionaryId, requestBody)).thenReturn(runningStatus);

        MvcResult result = mockMvc.perform(post("/repetition/choice/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(runningStatus));
    }

    @Test
    @SneakyThrows
    void testStart_WhenRepetitionIsAlreadyRunning_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(choiceRepetitionService.start(dictionaryId, requestBody))
                .thenThrow(new RepetitionException("Repetition is already running"));

        mockMvc.perform(post("/repetition/choice/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("Repetition is already running"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testStart_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(choiceRepetitionService.start(dictionaryId, requestBody))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(post("/repetition/choice/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage")
                                .value(String.format(
                                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                                        Dictionary.class.getSimpleName(),
                                        dictionaryId)));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testStart_WhenNoWordsForRepetition_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest requestBody = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(choiceRepetitionService.start(dictionaryId, requestBody))
                .thenThrow(new RepetitionException("No words to repeat"));

        mockMvc.perform(post("/repetition/choice/{dictionaryId}", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage").value("No words to repeat"));

        TimeZoneContextHolder.clear();
    }
}
