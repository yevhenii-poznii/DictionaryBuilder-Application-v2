package com.kiskee.dictionarybuilder.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.user.ProfileVisibility;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.model.dto.user.preference.DictionaryPreference;
import com.kiskee.dictionarybuilder.model.dto.user.preference.UserPreferenceDto;
import com.kiskee.dictionarybuilder.service.user.preference.UserPreferenceService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.time.Duration;
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

    @Test
    @SneakyThrows
    void testUpdateUserPreference_WhenProfileExists_ThenReturnUserPreferenceDto() {
        UserPreferenceDto requestBody = new UserPreferenceDto(
                ProfileVisibility.PUBLIC, 100, true, PageFilter.BY_ADDED_AT_ASC, 10, 10, Duration.ofHours(1));

        when(userPreferenceService.updateUserPreference(requestBody)).thenReturn(requestBody);

        MvcResult result = mockMvc.perform(put("/user/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(requestBody));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidUpdatePreferenceBody")
    void testUpdateUserPreference_WhenGivenInvalidBody_ThenBadRequest(UserPreferenceDto body) {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        mockMvc.perform(put("/user/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateUserPreference_WhenGivenInvalidProfileVisibility_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");
        String body = "{\"profileVisibility\":\"some wrong value\"";

        mockMvc.perform(put("/user/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateUserPreference_WhenGivenInvalidPageFilter_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");
        String body = "{\"pageFilter\":\"some wrong value\"";

        mockMvc.perform(put("/user/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateUserPreference_WhenGivenInvalidDailyRepetitionDurationGoal_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");
        String body = "{\"dailyRepetitionDurationGoal\":\"some wrong value\"";

        mockMvc.perform(put("/user/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateUserPreference_WhenUserPreferenceDoesNotExist_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        UserPreferenceDto requestBody = new UserPreferenceDto(
                ProfileVisibility.PUBLIC, 100, true, PageFilter.BY_ADDED_AT_ASC, 10, 10, Duration.ofHours(1));

        when(userPreferenceService.updateUserPreference(requestBody))
                .thenThrow(new ResourceNotFoundException(
                        String.format("%s [%s] hasn't been found", UserPreferenceDto.class.getSimpleName(), "userId")));

        mockMvc.perform(put("/user/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isNotFound());

        TimeZoneContextHolder.clear();
    }

    static Stream<UserPreferenceDto> invalidUpdatePreferenceBody() {
        return Stream.of(
                new UserPreferenceDto(null, 101, null, null, null, null, null),
                new UserPreferenceDto(null, 19, null, null, null, null, null),
                new UserPreferenceDto(null, null, null, null, 0, null, null),
                new UserPreferenceDto(null, null, null, null, null, 0, null));
    }
}
