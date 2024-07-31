package com.kiskee.dictionarybuilder.web.controller.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.model.dto.report.BaseReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.ReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.goal.word.WordAdditionGoalReportDto;
import com.kiskee.dictionarybuilder.service.report.goal.word.WordAdditionGoalReportService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.time.LocalDate;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebMvcTest(WordAdditionGoalReportController.class)
public class WordAdditionGoalReportControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private WordAdditionGoalReportService wordAdditionGoalReportService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testGetReport_WhenReportExists_ThenReturnReportDto() {
        ReportDto reportDto = new WordAdditionGoalReportDto(List.of(
                new BaseReportRowDto(LocalDate.of(2024, 7, 18), LocalDate.of(2024, 7, 18), 1, "daily", List.of())));

        when(wordAdditionGoalReportService.getReport()).thenReturn(reportDto);

        MvcResult result = mockMvc.perform(get("/report/word-addition"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(reportDto));
    }

    @Test
    @SneakyThrows
    void testGetReport_WhenReportDoesNotExist_ThenReturnNotFoundStatus() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        when(wordAdditionGoalReportService.getReport())
                .thenThrow(new ResourceNotFoundException("There is no report yet"));

        mockMvc.perform(get("/report/word-addition"))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("There is no report yet"));

        TimeZoneContextHolder.clear();
    }
}
