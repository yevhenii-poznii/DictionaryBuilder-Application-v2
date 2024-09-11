package com.kiskee.dictionarybuilder.web.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.exception.user.DuplicateUserException;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;
import com.kiskee.dictionarybuilder.service.user.profile.UserProfileService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.time.Instant;
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
@WebMvcTest(UserProfileController.class)
public class UserProfileControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testGetMiniProfile_WhenProfileExists_ThenReturnUserMiniProfileDto() {
        UserMiniProfileDto miniProfile = new UserMiniProfileDto("username", "someEncodedPicture");

        when(userProfileService.getMiniProfile()).thenReturn(miniProfile);

        MvcResult result = mockMvc.perform(get("/user/profile/me/mini"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(miniProfile));
    }

    @Test
    @SneakyThrows
    void testGetFullProfile_WhenProfileExists_ThenReturnUserProfileDto() {
        UserProfileDto profileDto = new UserProfileDto(
                "username", "public name", "someEncodedPicture", Instant.parse("2024-08-20T12:45:33Z"));

        when(userProfileService.getFullProfile()).thenReturn(profileDto);

        MvcResult result = mockMvc.perform(get("/user/profile/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(profileDto));
    }

    @Test
    @SneakyThrows
    void testUpdateProfile_WhenProfileExists_ThenReturnUserProfileDto() {
        UpdateUserProfileDto requestBody = new UpdateUserProfileDto("newPublicUsername", "new name", "new avatar");

        UserProfileDto profileDto = new UserProfileDto(
                requestBody.publicUsername(),
                requestBody.publicName(),
                requestBody.profilePicture(),
                Instant.parse("2024-08-20T12:45:33Z"));

        when(userProfileService.updateProfile(requestBody)).thenReturn(profileDto);

        MvcResult result = mockMvc.perform(put("/user/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(profileDto));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidUsername")
    void testUpdateProfile_WhenGivenInvalidNewUsername_ThenReturnBadRequest(String invalidUsername) {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        UpdateUserProfileDto requestBody = new UpdateUserProfileDto(invalidUsername, "new name", "new avatar");

        mockMvc.perform(put("/user/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateProfile_WhenNewUsernameAlreadyExists_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        UpdateUserProfileDto requestBody = new UpdateUserProfileDto("newPublicUsername", "new name", "new avatar");

        when(userProfileService.updateProfile(requestBody))
                .thenThrow(new DuplicateUserException(
                        String.format("Username \"%s\" already exists", requestBody.publicUsername())));

        mockMvc.perform(put("/user/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        TimeZoneContextHolder.clear();
    }

    static Stream<String> invalidUsername() {
        return Stream.of("us", "p#Ssword1", "email@gmail.com", "username with spaces");
    }
}
