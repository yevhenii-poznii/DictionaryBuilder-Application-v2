package com.kiskee.dictionarybuilder.web.controller.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.share.ShareDictionaryRequest;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryPage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.service.share.ShareService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.time.Instant;
import java.util.List;
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
@WebMvcTest(ShareController.class)
public class ShareControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private ShareService shareService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void testGetSharedDictionaryPage_WhenGivenValidSharingToken_ThenReturnSharedDictionaryPage() {
        String sharingToken = "sharingToken";
        DictionaryPageRequestDto pageRequestDto = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        String dictionaryName = "dictionaryName";
        List<WordDto> words =
                List.of(new WordDto(1L, "word1", true, null, 1, null), new WordDto(2L, "word2", true, null, 1, null));
        SharedDictionaryPage sharedDictionaryPage =
                new SharedDictionaryPage(dictionaryName, new DictionaryPageResponseDto(words, 0, 100));
        when(shareService.getSharedDictionaryPage(sharingToken, pageRequestDto)).thenReturn(sharedDictionaryPage);

        MvcResult result = mockMvc.perform(get("/share/{sharingToken}", sharingToken)
                        .param("page", pageRequestDto.getPage().toString())
                        .param("size", pageRequestDto.getSize().toString())
                        .param("filter", pageRequestDto.getFilter().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(sharedDictionaryPage));
    }

    @Test
    @SneakyThrows
    void testGetSharedDictionaryPage_WhenGivenInvalidSharingToken_ThenReturnForbiddenStatus() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        String sharingToken = "sharingToken";
        DictionaryPageRequestDto pageRequestDto = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        when(shareService.getSharedDictionaryPage(sharingToken, pageRequestDto))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(get("/share/{sharingToken}", sharingToken)
                        .param("page", pageRequestDto.getPage().toString())
                        .param("size", pageRequestDto.getSize().toString())
                        .param("filter", pageRequestDto.getFilter().toString()))
                .andDo(print())
                .andExpect(status().isForbidden());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testGetSharedDictionaryPage_WhenDictionaryDoesNotExistBySharingToken_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        String sharingToken = "sharingToken";
        DictionaryPageRequestDto pageRequestDto = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        when(shareService.getSharedDictionaryPage(sharingToken, pageRequestDto))
                .thenThrow(new ResourceNotFoundException("Dictionary [10] hasn't been found"));

        mockMvc.perform(get("/share/{sharingToken}", sharingToken)
                        .param("page", pageRequestDto.getPage().toString())
                        .param("size", pageRequestDto.getSize().toString())
                        .param("filter", pageRequestDto.getFilter().toString()))
                .andDo(print())
                .andExpect(status().isNotFound());

        TimeZoneContextHolder.clear();
    }

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("provideInvalidDictionaryPageRequestDto")
    void testGetSharedDictionaryPage_WhenGivenInvalidParams_ThenReturnBadRequest(
            DictionaryPageRequestDto requestParams) {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        String sharingToken = "sharingToken";

        mockMvc.perform(get("/share/{sharingToken}", sharingToken)
                        .param("page", requestParams.getPage().toString())
                        .param("size", requestParams.getSize().toString())
                        .param("filter", requestParams.getFilter().toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testShareDictionary_WhenGivenValidBody_ThenReturnSharedDictionaryDto() {
        Instant shareToDate = Instant.MAX;
        ShareDictionaryRequest shareDictionaryRequest = new ShareDictionaryRequest(10L, shareToDate);

        SharedDictionaryDto sharedDictionaryDto = new SharedDictionaryDto(10L, "sharingToken", shareToDate);
        when(shareService.shareDictionary(shareDictionaryRequest)).thenReturn(sharedDictionaryDto);

        MvcResult result = mockMvc.perform(post("/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shareDictionaryRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(sharedDictionaryDto));
    }

    @Test
    @SneakyThrows
    void testShareDictionary_WhenDictionaryDoesNotExist_ThenReturnNotFound() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        Instant shareToDate = Instant.MAX;
        ShareDictionaryRequest shareDictionaryRequest = new ShareDictionaryRequest(10L, shareToDate);

        when(shareService.shareDictionary(shareDictionaryRequest))
                .thenThrow(new ResourceNotFoundException("Dictionary [10] hasn't been found"));

        mockMvc.perform(post("/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shareDictionaryRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        TimeZoneContextHolder.clear();
    }

    @Test
    @SneakyThrows
    void testShareDictionary_WhenSharingTokenAlreadyExists_ThenReturnBadRequest() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        Instant shareToDate = Instant.MAX;
        ShareDictionaryRequest shareDictionaryRequest = new ShareDictionaryRequest(10L, shareToDate);

        when(shareService.shareDictionary(shareDictionaryRequest))
                .thenThrow(new DuplicateResourceException("SharingToken already exists to specified date"));

        mockMvc.perform(post("/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shareDictionaryRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("provideInvalidShareDictionaryRequestBody")
    void testShareDictionary_WhenGivenInvalidBody_ThenReturnBadRequest(ShareDictionaryRequest body) {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        mockMvc.perform(post("/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        TimeZoneContextHolder.clear();
    }

    private static Stream<DictionaryPageRequestDto> provideInvalidDictionaryPageRequestDto() {
        return Stream.of(
                new DictionaryPageRequestDto(-1, 100, PageFilter.BY_ADDED_AT_ASC),
                new DictionaryPageRequestDto(0, 101, PageFilter.BY_ADDED_AT_ASC),
                new DictionaryPageRequestDto(-1, 101, PageFilter.BY_ADDED_AT_ASC));
    }

    private static Stream<ShareDictionaryRequest> provideInvalidShareDictionaryRequestBody() {
        return Stream.of(
                new ShareDictionaryRequest(-1L, Instant.MAX),
                new ShareDictionaryRequest(1L, Instant.MIN),
                new ShareDictionaryRequest(-1L, Instant.MIN));
    }
}
