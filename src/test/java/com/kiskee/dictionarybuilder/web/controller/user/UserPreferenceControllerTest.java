package com.kiskee.dictionarybuilder.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.model.dto.user.preference.DictionaryPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.service.user.preference.UserPreferenceService;
import java.time.Duration;
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
@WebMvcTest(UserPreferenceController.class)
public class UserPreferenceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private UserPreferenceService userPreferenceService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testGetUserPreference_WhenPreferenceExists_ThenReturnUserPreferenceDto() {
        UserPreferenceDto userPreferenceDto = new UserPreferenceDto(
                ProfileVisibility.PUBLIC, 100, true, PageFilter.BY_ADDED_AT_ASC, 10, 10, Duration.ofHours(1));

        when(userPreferenceService.getUserPreference()).thenReturn(userPreferenceDto);

        MvcResult result = mockMvc.perform(get("/user/preference"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(userPreferenceDto));
    }

    @Test
    @SneakyThrows
    void testGetDictionaryPreference_WhenPreferenceExists_ThenReturnUserDictionaryPreferenceDto() {
        DictionaryPreference dictionaryPreference = new DictionaryPreference(100, true, PageFilter.BY_ADDED_AT_ASC);

        when(userPreferenceService.getDictionaryPreference()).thenReturn(dictionaryPreference);

        MvcResult result = mockMvc.perform(get("/user/preference/dictionary"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(dictionaryPreference));
    }
}
