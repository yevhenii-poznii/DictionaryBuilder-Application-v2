package com.kiskee.vocabulary.web.controller.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.ForbiddenAccessException;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.service.vocabulary.word.WordServiceImpl;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

@WebMvcTest(WordController.class)
@ExtendWith(SpringExtension.class)
public class WordControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private WordServiceImpl wordService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @BeforeAll
    static void beforeAll() {
        TimeZoneContextHolder.setTimeZone("UTC");
    }

    @AfterAll
    static void afterAll() {
        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testAdd_WhenDictionaryExistsForUserAndGivenValidWordSaveRequest_ThenAddNewWord() {
        Long dictionaryId = 1L;
        String word = "word";
        WordSaveRequest saveRequest = new WordSaveRequest(
                word,
                List.of(new WordTranslationDto(null, "переклад"), new WordTranslationDto(null, "перекладд")),
                "hint");

        WordSaveResponse expectedResponse = new WordSaveResponse(
                String.format(VocabularyResponseMessageEnum.WORD_ADDED.getResponseMessage(), word),
                new WordDto(
                        1L,
                        word,
                        true,
                        Set.of(new WordTranslationDto(1L, "переклад"), new WordTranslationDto(2L, "перекладд")),
                        0,
                        "hint"));
        when(wordService.addWord(dictionaryId, saveRequest)).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(post("/dictionaries/{dictionaryId}/words", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(new String(actualResponseBody.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
                .isEqualTo(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    @SneakyThrows
    void testAdd_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound() {
        Long dictionaryId = 1L;
        String word = "word";
        WordSaveRequest saveRequest = new WordSaveRequest(
                word,
                List.of(new WordTranslationDto(null, "переклад"), new WordTranslationDto(null, "переклад")),
                "hint");

        when(wordService.addWord(dictionaryId, saveRequest))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(post("/dictionaries/{dictionaryId}/words", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidWordSaveRequest")
    void testAdd_WhenGivenInvalidWordSaveRequest_ThenReturnBadRequest(WordSaveRequest saveRequest) {
        long dictionaryId = 1L;

        mockMvc.perform(post("/dictionaries/{dictionaryId}/words", dictionaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andDo(print())
                .andExpectAll(status().isBadRequest(), jsonPath("$.status").value("Bad Request"))
                .andReturn();
    }

    @Test
    @SneakyThrows
    void testUpdate_WhenDictionaryExistsForUserAndGivenValidWordUpdateRequest_ThenUpdateExistingWord() {
        long dictionaryId = 1L;
        long wordId = 10L;
        String word = "word";
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                word,
                List.of(new WordTranslationDto(1L, "перекладдд"), new WordTranslationDto(2L, "перекладд")),
                "hinthint");

        WordSaveResponse expectedResponse = new WordSaveResponse(
                String.format(VocabularyResponseMessageEnum.WORD_UPDATED.getResponseMessage(), word),
                new WordDto(
                        10L,
                        word,
                        false,
                        Set.of(new WordTranslationDto(1L, "перекладдд"), new WordTranslationDto(2L, "перекладд")),
                        0,
                        "hinthint"));
        when(wordService.updateWord(dictionaryId, wordId, updateRequest)).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(put("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(new String(actualResponseBody.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
                .isEqualTo(objectMapper.writeValueAsString(expectedResponse));
    }

    @Test
    @SneakyThrows
    void testUpdate_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound() {
        long dictionaryId = 1L;
        long wordId = 10L;
        String word = "word";
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                word,
                List.of(new WordTranslationDto(1L, "перекладдд"), new WordTranslationDto(2L, "перекладд")),
                "hinthint");

        when(wordService.updateWord(dictionaryId, wordId, updateRequest))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(put("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));
    }

    @Test
    @SneakyThrows
    void testUpdate_WhenWordDoesNotExistForDictionary_ThenReturnForbiddenStatus() {
        long dictionaryId = 1L;
        long wordId = 10L;
        String word = "word";
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                word,
                List.of(new WordTranslationDto(1L, "перекладдд"), new WordTranslationDto(2L, "перекладд")),
                "hinthint");

        when(wordService.updateWord(dictionaryId, wordId, updateRequest))
                .thenThrow(new ForbiddenAccessException(String.format(
                        ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), wordId)));

        mockMvc.perform(put("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value("Forbidden"),
                        jsonPath("$.errors.responseMessage").value("You have no access to Word [10]"));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("invalidWordUpdateRequest")
    void testUpdate_WhenGivenInvalidWordSaveRequest_ThenReturnBadRequest(WordUpdateRequest updateRequest) {
        long dictionaryId = 1L;
        long wordId = 10L;

        mockMvc.perform(put("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpectAll(status().isBadRequest(), jsonPath("$.status").value("Bad Request"))
                .andReturn();
    }

    @Test
    @SneakyThrows
    void testDelete_WhenDictionaryAndWordExistForUser_ThenDeleteWord() {
        long dictionaryId = 1L;
        long wordId = 10L;

        Word wordToDelete = mock(Word.class);
        when(wordToDelete.getWord()).thenReturn("word");
        ResponseMessage responseMessage = new ResponseMessage(
                String.format(VocabularyResponseMessageEnum.WORD_DELETED.getResponseMessage(), wordToDelete.getWord()));

        when(wordService.deleteWord(dictionaryId, wordId)).thenReturn(responseMessage);

        mockMvc.perform(delete("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId))
                .andDo(print())
                .andExpectAll(
                        status().isOk(), jsonPath("$.responseMessage").value(responseMessage.getResponseMessage()));
    }

    @Test
    @SneakyThrows
    void testDelete_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound() {
        long dictionaryId = 1L;
        long wordId = 10L;

        when(wordService.deleteWord(dictionaryId, wordId))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(delete("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));
    }

    @Test
    @SneakyThrows
    void testDelete_WhenWordDoesNotExistForDictionary_ThenReturnForbiddenStatus() {
        long dictionaryId = 1L;
        long wordId = 10L;

        when(wordService.deleteWord(dictionaryId, wordId))
                .thenThrow(new ForbiddenAccessException(String.format(
                        ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), wordId)));

        mockMvc.perform(delete("/dictionaries/{dictionaryId}/words/{wordId}", dictionaryId, wordId))
                .andDo(print())
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value("Forbidden"),
                        jsonPath("$.errors.responseMessage").value("You have no access to Word [10]"));
    }

    @Test
    @SneakyThrows
    void testDeleteSet_WhenDictionaryAndWordsExistForUser_ThenDeleteWords() {
        long dictionaryId = 1L;

        ResponseMessage responseMessage = new ResponseMessage("Words [10, 11, 12] have been deleted");

        when(wordService.deleteWords(dictionaryId, Set.of(10L, 11L, 12L))).thenReturn(responseMessage);

        mockMvc.perform(delete("/dictionaries/{dictionaryId}/words", dictionaryId)
                        .param("wordIds", "10", "11", "12"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(), jsonPath("$.responseMessage").value(responseMessage.getResponseMessage()));
    }

    @Test
    @SneakyThrows
    void testDeleteSet_WhenDictionaryDoesNotExistForUser_ThenReturnNotFound() {
        long dictionaryId = 1L;
        Set<Long> wordIds = Set.of(10L, 11L, 12L);

        when(wordService.deleteWords(dictionaryId, wordIds))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        mockMvc.perform(delete("/dictionaries/{dictionaryId}/words", dictionaryId)
                        .param("wordIds", "10", "11", "12"))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.errors.responseMessage").value("Dictionary [1] hasn't been found"));
    }

    @Test
    @SneakyThrows
    void testDeleteSets_WhenWordsDoNotExistForDictionary_ThenReturnForbiddenStatus() {
        long dictionaryId = 1L;
        Set<Long> wordIds = Set.of(10L, 11L, 12L);

        when(wordService.deleteWords(dictionaryId, wordIds))
                .thenThrow(new ForbiddenAccessException(String.format(
                        ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), "10")));

        mockMvc.perform(delete("/dictionaries/{dictionaryId}/words", dictionaryId)
                        .param("wordIds", "10", "11", "12"))
                .andDo(print())
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.errors.responseMessage").value("You have no access to Word [10]"));
    }

    static Stream<WordUpdateRequest> invalidWordUpdateRequest() {
        return Stream.of(
                new WordUpdateRequest(null, null, "hint"),
                new WordUpdateRequest("", List.of(), "hint"),
                new WordUpdateRequest("word123", List.of(new WordTranslationDto(null, "translation")), "hint"));
    }

    static Stream<WordSaveRequest> invalidWordSaveRequest() {
        return Stream.of(
                new WordSaveRequest(null, null, "hint"),
                new WordSaveRequest("", List.of(), "hint"),
                new WordSaveRequest("word123", List.of(new WordTranslationDto(null, "translation")), "hint"));
    }
}
