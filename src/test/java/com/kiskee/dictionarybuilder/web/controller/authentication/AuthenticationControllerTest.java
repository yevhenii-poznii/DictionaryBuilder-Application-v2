package com.kiskee.dictionarybuilder.web.controller.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationRequest;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationResponse;
import com.kiskee.dictionarybuilder.service.authentication.AuthenticationService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testSignIn_WhenAuthenticationHasSet_ThenReturnAccessToken() {
        AuthenticationRequest requestBody = new AuthenticationRequest("login", "password");

        AuthenticationResponse expectedResponseBody =
                new AuthenticationResponse("someToken", Instant.parse("2024-02-01T00:00:00Z"));
        when(authenticationService.issueAccessToken()).thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(post("/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedResponseBody));
    }

    @Test
    @SneakyThrows
    void testSignIn_WhenAuthenticationHasNotSet_ThenReturn401Unauthorized() {
        AuthenticationRequest requestBody = new AuthenticationRequest("login", "password");

        TimeZoneContextHolder.setTimeZone("UTC");

        when(authenticationService.issueAccessToken())
                .thenThrow(new AuthenticationCredentialsNotFoundException("User is not authenticated"));

        mockMvc.perform(post("/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value("Unauthorized"),
                        jsonPath("$.errors.responseMessage").value("User is not authenticated"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testRefresh_WhenCookieInRequest_ThenReturnNewAccessToken() {
        Cookie cookie = new Cookie("RefreshAuthentication", "someToken");

        AuthenticationResponse expectedResponseBody =
                new AuthenticationResponse("someNewToken", Instant.parse("2024-02-01T00:00:00Z"));
        when(authenticationService.issueAccessToken(cookie.getValue())).thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedResponseBody));
    }

    @Test
    @SneakyThrows
    void testRefresh_WhenCookieInRequestIsNotPresent_ThenReturn400BadRequest() {
        mockMvc.perform(post("/auth/refresh")).andDo(print()).andExpect(status().isBadRequest());
    }
}
