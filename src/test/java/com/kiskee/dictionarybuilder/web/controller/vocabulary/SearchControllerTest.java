package com.kiskee.dictionarybuilder.web.controller.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import com.kiskee.dictionarybuilder.service.vocabulary.search.SearchServiceImpl;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(SearchController.class)
@ExtendWith(SpringExtension.class)
public class SearchControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private SearchServiceImpl wordService;

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
    void testSearch_WhenGivenValidSearchWord_ThenReturnWordProjectionList() {
        List<WordProjection> expectedWords = List.of(
                new WordDto(1L, "test", true, null, 0, "hint"),
                new WordDto(2L, "test2", true, Set.of(new WordTranslationDto(null, null)), 0, "hint2"));

        String searchWord = "test";
        when(wordService.search(searchWord)).thenReturn(expectedWords);

        MvcResult result = mockMvc.perform(get("/search").param("searchWord", searchWord))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = result.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualTo(objectMapper.writeValueAsString(expectedWords));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideInvalidSearchWord")
    void testSearch_WhenGivenInvalidSearchWord_ThenReturnBadRequest(String searchWord) {
        mockMvc.perform(get("/search").param("searchWord", searchWord))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    static Stream<String> provideInvalidSearchWord() {
        return Stream.of("", null, " ");
    }
}
