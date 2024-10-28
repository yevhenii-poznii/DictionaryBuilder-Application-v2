package com.kiskee.dictionarybuilder.web.controller.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import com.kiskee.dictionarybuilder.web.advice.ErrorResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testCreateDictionary_WhenGivenValidRequestBody_ThenReturnDictionarySaveResponseAndStatusCreated() {
        DictionarySaveRequest requestBody = new DictionarySaveRequest("dictionaryName");

        String responseMessage = String.format(
                VocabularyResponseMessageEnum.DICTIONARY_CREATED.getResponseMessage(), requestBody.getDictionaryName());
        DictionarySaveResponse expectedResponseBody =
                new DictionarySaveResponse(responseMessage, new DictionaryDetailDto(1L, "dictionaryName", 0, 0, 0));

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
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(),
                        requestBody.getDictionaryName())));

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
    void testGetPage_WhenGivenExistingDictionaryIdAndValidRequestParams_ThenReturnDictionaryPage() {
        Long dictionaryId = 1L;
        DictionaryPageRequestDto pageRequest = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        DictionaryPageResponseDto expectedResponseBody = returnDictionaryPageResponseDto();

        when(dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest))
                .thenReturn(expectedResponseBody);

        MvcResult result = mockMvc.perform(get("/dictionaries/" + dictionaryId)
                        .param("page", pageRequest.getPage().toString())
                        .param("size", pageRequest.getSize().toString())
                        .param("filter", pageRequest.getFilter().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedResponseBody));
    }

    @Test
    @SneakyThrows
    void testGetPage_WhenGivenNotExistingDictionaryIdForUser_ThenReturnNotFoundStatus() {
        TimeZoneContextHolder.setTimeZone("UTC");

        Long dictionaryId = 1L;
        DictionaryPageRequestDto pageRequest = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        when(dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(get("/dictionaries/" + dictionaryId)
                        .param("page", pageRequest.getPage().toString())
                        .param("size", pageRequest.getSize().toString())
                        .param("filter", pageRequest.getFilter().toString()))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));

        TimeZoneContextHolder.clear();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidRequestParams")
    void testGetPage_When_ThenReturnBadRequest(Tuple params) {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;
        List<Object> parameters = Arrays.stream(params.toArray()).toList();
        DictionaryPageRequestDto pageRequest = (DictionaryPageRequestDto) parameters.get(0);
        List<?> errors = (List<?>) parameters.get(1);

        when(dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest))
                .thenThrow(new IllegalArgumentException("Invalid page request"));

        MvcResult result = mockMvc.perform(get("/dictionaries/" + dictionaryId)
                        .param("page", pageRequest.getPage().toString())
                        .param("size", pageRequest.getSize().toString())
                        .param("filter", pageRequest.getFilter().toString()))
                .andDo(print())
                .andExpectAll(status().isBadRequest(), jsonPath("$.status").value("Bad Request"))
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(actualResponseBody, ErrorResponse.class);

        assertThat(errorResponse.errors())
                .extractingFromEntries(Map.Entry::getValue)
                .containsExactlyInAnyOrderElementsOf(
                        errors.stream().map(Object::toString).collect(Collectors.toList()));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testGetDictionaries_WhenUserHasTwoDictionaries_ThenReturnTwoDictionariesDtoAndStatusOk() {
        List<DictionaryDto> dictionaries =
                List.of(new DictionaryDto(1L, "Default Dictionary"), new DictionaryDto(2L, "Dictionary 1"));

        when(dictionaryService.getDictionaries()).thenReturn(dictionaries);

        mockMvc.perform(get("/dictionaries").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(status().isOk(), jsonPath("$.length()").value(2));
    }

    @Test
    @SneakyThrows
    void testGetDictionaries_WhenUserHasNoDictionaries_ThenReturnEmptyListAndStatusOk() {
        List<DictionaryDto> dictionaries = List.of();

        when(dictionaryService.getDictionaries()).thenReturn(dictionaries);

        mockMvc.perform(get("/dictionaries").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(status().isOk(), jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    void testGetDetailedDictionaries_WhenUserHasTwoDictionaries_ThenReturnTwoDictionariesDtoAndStatusOk() {
        List<DictionaryDetailDto> dictionaries = List.of(
                new DictionaryDetailDto(1L, "Default Dictionary", 0, 0, 0),
                new DictionaryDetailDto(2L, "Dictionary 1", 123, 23, 100));

        when(dictionaryService.getDetailedDictionaries()).thenReturn(dictionaries);

        mockMvc.perform(get("/dictionaries/detailed").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(status().isOk(), jsonPath("$.length()").value(2));
    }

    @Test
    @SneakyThrows
    void testGetDetailedDictionaries_WhenUserHasNoDictionaries_ThenReturnEmptyListAndStatusOk() {
        List<DictionaryDetailDto> dictionaries = List.of();

        when(dictionaryService.getDetailedDictionaries()).thenReturn(dictionaries);

        mockMvc.perform(get("/dictionaries/detailed").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(status().isOk(), jsonPath("$.length()").value(0));
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

        String responseMessage = String.format(
                VocabularyResponseMessageEnum.DICTIONARY_UPDATED.getResponseMessage(), requestBody.getDictionaryName());
        DictionarySaveResponse expectedResponseBody = new DictionarySaveResponse(
                responseMessage, new DictionaryDetailDto(dictionaryId, "dictionaryName", 0, 0, 0));

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
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(),
                        requestBody.getDictionaryName())));

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
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(put("/dictionaries/" + dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testDeleteDictionaries_WhenGivenDictionaryIdsExistForUser_ThenDeleteDictionariesAndReturnStatusOk() {
        Set<Long> dictionaryIds = Set.of(1L, 2L);

        when(dictionaryService.deleteDictionaries(dictionaryIds))
                .thenReturn(new ResponseMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARIES_DELETED.getResponseMessage(),
                        dictionaryIds.size())));

        mockMvc.perform(delete("/dictionaries")
                        .param("dictionaryIds", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(), jsonPath("$.responseMessage").value("Deleted 2 dictionaries successfully"));
    }

    @Test
    @SneakyThrows
    void testDeleteDictionaries_WhenGivenDictionaryIdsDoNotExistForUser_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("UTC");

        Set<Long> dictionaryIds = Set.of(1L, 2L);

        when(dictionaryService.deleteDictionaries(dictionaryIds))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCES_NOT_FOUND.getStatus(), "Dictionaries", dictionaryIds)));

        mockMvc.perform(delete("/dictionaries")
                        .param("dictionaryIds", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage")
                                .value(String.format(
                                        ExceptionStatusesEnum.RESOURCES_NOT_FOUND.getStatus(),
                                        "Dictionaries",
                                        dictionaryIds)));

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testDeleteDictionaries_WhenGivenNoDictionaryIds_ThenReturnBadRequest() {
        mockMvc.perform(delete("/dictionaries").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testDeleteDictionary_WhenGivenDictionaryIdExistsForUser_ThenReturnResponseAndStatusOk() {
        long dictionaryId = 1L;

        when(dictionaryService.deleteDictionary(dictionaryId))
                .thenReturn(new ResponseMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_DELETED.getResponseMessage(), "dictionaryName")));

        mockMvc.perform(delete("/dictionaries/" + dictionaryId).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.responseMessage").value("dictionaryName dictionary deleted successfully"));
    }

    @Test
    @SneakyThrows
    void testDeleteDictionary_WhenGivenDictionaryIdDoesNotExistForUser_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("UTC");

        long dictionaryId = 1L;

        when(dictionaryService.deleteDictionary(dictionaryId))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(delete("/dictionaries/" + dictionaryId).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));

        TimeZoneContextHolder.clear();
    }

    static Stream<String> invalidRequestBody() {
        return Stream.of(
                null,
                "",
                " ",
                "MoreThan50CharsMoreThan50CharsMoreThan50CharsMoreThan50CharsMoreThan50Chars",
                "dictionaryName!",
                "dictionaryName@",
                "dictionaryName#",
                "dictionaryName$",
                "dictionaryName%",
                "dictionaryName ",
                " dictionaryName");
    }

    static Stream<Tuple> invalidRequestParams() {
        return Stream.of(
                Tuple.tuple(
                        new DictionaryPageRequestDto(-1, 100, PageFilter.BY_ADDED_AT_ASC),
                        List.of("must be greater than or equal to 0")),
                Tuple.tuple(
                        new DictionaryPageRequestDto(0, 19, PageFilter.BY_ADDED_AT_ASC),
                        List.of("must be greater than or equal to 20")),
                Tuple.tuple(
                        new DictionaryPageRequestDto(-1, 19, PageFilter.BY_ADDED_AT_ASC),
                        List.of("must be greater than or equal to 0", "must be greater than or equal to 20")));
    }

    private static DictionaryPageResponseDto returnDictionaryPageResponseDto() {
        List<WordDto> words = List.of(
                new WordDto(
                        1L,
                        "word1",
                        true,
                        Set.of(new WordTranslationDto(1L, "translation1"), new WordTranslationDto(2L, "translation2")),
                        0,
                        "hint1"),
                new WordDto(
                        2L,
                        "word2",
                        false,
                        Set.of(new WordTranslationDto(3L, "translation3"), new WordTranslationDto(4L, "translation4")),
                        0,
                        "hint2"));
        return new DictionaryPageResponseDto(words, 0, 2);
    }
}
