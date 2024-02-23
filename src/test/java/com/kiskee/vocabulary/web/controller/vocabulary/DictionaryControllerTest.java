package com.kiskee.vocabulary.web.controller.vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.DuplicateResourceException;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryCreationService;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DictionaryController.class)
public class DictionaryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @MockBean
    private DictionaryCreationService dictionaryService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testCreateDictionary_WhenGivenValidRequestBody_ThenReturnDictionarySaveResponseAndStatusCreated() {
        DictionarySaveRequest requestBody = new DictionarySaveRequest("dictionaryName");

        String responseMessage = String.format(VocabularyResponseMessageEnum.DICTIONARY_CREATED.getResponseMessage(),
                requestBody.getDictionaryName());
        DictionarySaveResponse expectedResponseBody = new DictionarySaveResponse(responseMessage,
                new DictionaryDto(1L, "dictionaryName", 0));

        when(dictionaryService.addDictionary(requestBody)).thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(post("/dictionary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedResponseBody));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidRequestBody")
    void testCreateDictionary_WhenGivenInvalidRequestBody_ThenReturnBadRequest(String dictionaryName) {
        TimeZoneContextHolder.setTimeZone("UTC");

        DictionarySaveRequest requestBody = new DictionarySaveRequest(dictionaryName);

        mockMvc.perform(post("/dictionary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testCreateDictionary_WhenGivenDictionaryNameAlreadyExistsForUser_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        DictionarySaveRequest requestBody = new DictionarySaveRequest("dictionaryName");

        when(dictionaryService.addDictionary(requestBody))
                .thenThrow(new DuplicateResourceException(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), requestBody.getDictionaryName())));

        mockMvc.perform(post("/dictionary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage")
                                .value("Dictionary with name dictionaryName already exists for user"));

        TimeZoneContextHolder.clear();
    }

    static Stream<String> invalidRequestBody() {
        return Stream.of(
                null, "", " ", "MoreThan50CharsMoreThan50CharsMoreThan50CharsMoreThan50CharsMoreThan50Chars",
                "dictionaryName!", "dictionaryName@", "dictionaryName#", "dictionaryName$", "dictionaryName%",
                "dictionaryName ", " dictionaryName"
        );
    }

}
