package com.kiskee.vocabulary.web.controller.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterRequestDto;
import com.kiskee.vocabulary.model.dto.registration.UserRegisterResponseDto;
import com.kiskee.vocabulary.service.registration.RegistrationService;
import com.kiskee.vocabulary.web.advice.ErrorResponse;
import lombok.SneakyThrows;
import org.assertj.core.groups.Tuple;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RegistrationController.class)
public class RegistrationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @MockBean
    private RegistrationService registrationService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testSingUp_WhenRequestBodyValid_ThenRegisterUserAccountAndReturnStatusCreated() {
        UserRegisterRequestDto requestBody = new UserRegisterRequestDto("email@gmail.com", "username",
                "p#Ssword1", null);

        UserRegisterResponseDto expectedResponseBody = new UserRegisterResponseDto(String.format(
                RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(), requestBody.getEmail()));
        when(registrationService.registerUserAccount(requestBody)).thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedResponseBody));
    }

    @Test
    @SneakyThrows
    void testSingUp_WhenRequestBodyValidAndUserWithTheSameEmailOrUsernameAlreadyExists_ThenReturnStatus422() {
        UserRegisterRequestDto requestBody = new UserRegisterRequestDto("email@gmail.com", "username",
                "p@Ssword1", null);

        when(registrationService.registerUserAccount(requestBody))
                .thenThrow(new DuplicateUserException(RegistrationStatus.USER_ALREADY_EXISTS.getStatus()));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        jsonPath("$.status").value("Unprocessable Entity"),
                        jsonPath("$.errors.responseMessage")
                                .value("User with the same email or username already exists.")
                );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidRequestBody")
    void testSingUp_WhenRequestBodyInvalid_ThenReturnStatus400(Tuple invalidRequestBody) {
        List<Object> parameters = Arrays.stream(invalidRequestBody.toArray()).toList();
        UserRegisterRequestDto userRegisterRequestDto = (UserRegisterRequestDto) parameters.get(0);
        List<?> errors = (List<?>) parameters.get(1);

        MvcResult result = mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterRequestDto)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value("Bad Request")
                )
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);

        assertThat(errorResponse.getErrors())
                .extractingFromEntries(Map.Entry::getValue)
                .containsExactlyElementsOf(errors.stream().map(Object::toString).collect(Collectors.toList()));
    }

    static Stream<Tuple> invalidRequestBody() {
        return Stream.of(
                Tuple.tuple(new UserRegisterRequestDto("em", "username", "p#Ssword1",
                        null), List.of("Email must be a valid")),
                Tuple.tuple(new UserRegisterRequestDto("email@gmail.com", "us", "p#Ssword1",
                        null), List.of("Invalid username format. Only letters, numbers, underscore (_), hyphen (-), and dot (.) are allowed.")),
                Tuple.tuple(new UserRegisterRequestDto("email@gmail.com", "username", "pass",
                        null), List.of("Password size must be between 8 and 50 chars, must contain at least one lowercase letter, one uppercase letter, one digit, one special character, and should not contain spaces.")),
                Tuple.tuple(new UserRegisterRequestDto(null, null, null,
                        null), List.of("must not be blank", "Email cannot be empty",
                        "must not be blank"))
        );
    }

}
