package com.kiskee.vocabulary.web.controller.vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.DuplicateResourceException;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryService;
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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private DictionaryService dictionaryService;
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

        MvcResult result = mockMvc.perform(post("/dictionaries")
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

        mockMvc.perform(post("/dictionaries")
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

        mockMvc.perform(post("/dictionaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage")
                                .value("Dictionary with name dictionaryName already exists for user"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testGetDictionaries_WhenUserHasTwoDictionaries_ThenReturnTwoDictionariesDtoAndStatusOk() {
        List<DictionaryDto> dictionaries = List.of(
                new DictionaryDto(1L, "Default Dictionary", 0),
                new DictionaryDto(2L, "Dictionary 1", 123)
        );

        when(dictionaryService.getDictionaries()).thenReturn(dictionaries);

        mockMvc.perform(get("/dictionaries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(2)
                );
    }

    @Test
    @SneakyThrows
    void testGetDictionaries_WhenUserHasNoDictionaries_ThenReturnEmptyListAndStatusOk() {
        List<DictionaryDto> dictionaries = List.of();

        when(dictionaryService.getDictionaries()).thenReturn(dictionaries);

        mockMvc.perform(get("/dictionaries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(0)
                );
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidRequestBody")
    void testUpdateDictionary_WhenGivenInvalidRequestBody_ThenReturnBadRequest(String dictionaryName) {
        TimeZoneContextHolder.setTimeZone("UTC");

        DictionarySaveRequest requestBody = new DictionarySaveRequest(dictionaryName);

        mockMvc.perform(put("/dictionaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateDictionary_WhenGivenValidRequestBody_ThenReturnDictionarySaveResponseAndStatusOk() {
        DictionarySaveRequest requestBody = new DictionarySaveRequest("dictionaryName");

        long dictionaryId = 1L;

        String responseMessage = String.format(VocabularyResponseMessageEnum.DICTIONARY_UPDATED.getResponseMessage(),
                requestBody.getDictionaryName());
        DictionarySaveResponse expectedResponseBody = new DictionarySaveResponse(responseMessage,
                new DictionaryDto(dictionaryId, "dictionaryName", 0));

        when(dictionaryService.updateDictionary(dictionaryId, requestBody)).thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(put("/dictionaries/" + dictionaryId)
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
    void testUpdateDictionary_WhenGivenDictionaryNameAlreadyExistsForUser_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("UTC");

        DictionarySaveRequest requestBody = new DictionarySaveRequest("dictionaryName");

        long dictionaryId = 1L;

        when(dictionaryService.updateDictionary(dictionaryId, requestBody))
                .thenThrow(new DuplicateResourceException(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), requestBody.getDictionaryName())));

        mockMvc.perform(put("/dictionaries/" + dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.responseMessage")
                                .value("Dictionary with name dictionaryName already exists for user"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testUpdateDictionary_WhenGivenDictionaryIdDoesNotExistForUser_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("UTC");

        DictionarySaveRequest requestBody = new DictionarySaveRequest("dictionaryName");

        long dictionaryId = 1L;

        when(dictionaryService.updateDictionary(dictionaryId, requestBody))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId)));

        mockMvc.perform(put("/dictionaries/" + dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage")
                                .value("Dictionary [1] hasn't been found"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testDeleteDictionary_WhenGivenDictionaryIdExistsForUser_ThenReturnResponseAndStatusOk() {
        long dictionaryId = 1L;

        when(dictionaryService.deleteDictionary(dictionaryId))
                .thenReturn(new ResponseMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_DELETED.getResponseMessage(), "dictionaryName")));

        mockMvc.perform(delete("/dictionaries/" + dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.responseMessage")
                                .value("dictionaryName dictionary deleted successfully"));
    }

    @Test
    @SneakyThrows
    void testDeleteDictionary_WhenGivenDictionaryIdDoesNotExistForUser_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;

        when(dictionaryService.deleteDictionary(dictionaryId))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId)));

        mockMvc.perform(delete("/dictionaries/" + dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage")
                                .value("Dictionary [1] hasn't been found"));

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
