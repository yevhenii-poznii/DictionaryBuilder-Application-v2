package com.kiskee.vocabulary.web.controller.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.registration.RegistrationStatus;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.token.InvalidVerificationTokenException;
import com.kiskee.vocabulary.exception.user.DuplicateUserException;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.registration.InternalRegistrationRequest;
import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.service.provision.registration.RegistrationService;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import com.kiskee.vocabulary.web.advice.ErrorResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testSingUp_WhenRequestBodyValid_ThenRegisterUserAccountAndReturnStatusCreated() {
        InternalRegistrationRequest requestBody =
                new InternalRegistrationRequest("email@gmail.com", "username", "p#Ssword1");

        ResponseMessage expectedResponseBody = new ResponseMessage(
                String.format(RegistrationStatus.USER_SUCCESSFULLY_CREATED.getStatus(), requestBody.getEmail()));
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
        InternalRegistrationRequest requestBody =
                new InternalRegistrationRequest("email@gmail.com", "username", "p#Ssword1");

        TimeZoneContextHolder.setTimeZone("UTC");

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
                                .value("User with the same email or username already exists."));

        TimeZoneContextHolder.clear();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidRequestBody")
    void testSingUp_WhenRequestBodyInvalid_ThenReturnStatus400(Tuple invalidRequestBody) {
        List<Object> parameters = Arrays.stream(invalidRequestBody.toArray()).toList();
        RegistrationRequest registrationRequest = (RegistrationRequest) parameters.get(0);
        List<?> errors = (List<?>) parameters.get(1);

        TimeZoneContextHolder.setTimeZone("UTC");

        MvcResult result = mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Time-Zone", "UTC")
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andDo(print())
                .andExpectAll(status().isBadRequest(), jsonPath("$.status").value("Bad Request"))
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);

        assertThat(errorResponse.errors())
                .extractingFromEntries(Map.Entry::getValue)
                .containsExactlyElementsOf(errors.stream().map(Object::toString).collect(Collectors.toList()));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testConfirmRegistration_WhenGivenCorrectVerificationTokenRequestParam_ThenReturnStatusOkAndResponseMessage() {
        String verificationTokenRequestParam = "some_verification_token";

        ResponseMessage expectedResponseBody =
                new ResponseMessage(RegistrationStatus.USER_SUCCESSFULLY_ACTIVATED.getStatus());
        when(registrationService.completeRegistration(verificationTokenRequestParam))
                .thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(patch("/signup/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("verificationToken", verificationTokenRequestParam))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedResponseBody));
    }

    @Test
    @SneakyThrows
    void testConfirmRegistration_WhenVerificationTokenRequestParamNotProvided_ThenReturnBadRequestStatus() {
        mockMvc.perform(patch("/signup/activate").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    void testConfirmRegistration_WhenVerificationTokenOrUserNotFound_ThenReturnNotFoundStatusAndResponseMessage() {
        String verificationTokenRequestParam = "some_verification_token";

        TimeZoneContextHolder.setTimeZone("UTC");

        when(registrationService.completeRegistration(verificationTokenRequestParam))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        UserVocabularyApplication.class.getSimpleName(),
                        "userId")));

        mockMvc.perform(patch("/signup/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("verificationToken", verificationTokenRequestParam))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value("Not Found"),
                        jsonPath("$.errors.responseMessage")
                                .value("UserVocabularyApplication [userId] hasn't been found"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testConfirmRegistration_WhenGivenVerificationTokenIsAlreadyInvalid_ThenReturnConflictStatus() {
        String verificationTokenRequestParam = "some_verification_token";

        TimeZoneContextHolder.setTimeZone("UTC");

        when(registrationService.completeRegistration(verificationTokenRequestParam))
                .thenThrow(new InvalidVerificationTokenException("Verification token is already invalidated"));

        mockMvc.perform(patch("/signup/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("verificationToken", verificationTokenRequestParam))
                .andDo(print())
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status").value("Conflict"),
                        jsonPath("$.errors.responseMessage").value("Verification token is already invalidated"));

        TimeZoneContextHolder.clear();
    }

    static Stream<Tuple> invalidRequestBody() {
        return Stream.of(
                Tuple.tuple(
                        new InternalRegistrationRequest("em", "username", "p#Ssword1"),
                        List.of("Email must be a valid")),
                Tuple.tuple(
                        new InternalRegistrationRequest("email@gmail.com", "us", "p#Ssword1"),
                        List.of(
                                "Invalid username format. Only letters, numbers, underscore (_), hyphen (-), and dot (.) are allowed.")),
                Tuple.tuple(
                        new InternalRegistrationRequest("email@gmail.com", "username", "pass"),
                        List.of(
                                "Password size must be between 8 and 50 chars, must contain at least one lowercase letter, one uppercase letter, one digit, one special character, and should not contain spaces.")),
                Tuple.tuple(
                        new InternalRegistrationRequest(null, null, null),
                        List.of("must not be blank", "Email cannot be empty", "must not be blank")));
    }
}
