package com.kiskee.dictionarybuilder.service.vocabulary.loader.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.WordFilter;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.AllRepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.CountRepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.DateRepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderAllASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderOnlyNotUseInRepetitionASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderOnlyUseInRepetitionASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderAllDESC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderOnlyNotUseInRepetitionDESC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderOnlyUseInRepetitionDESC;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class WordLoaderFactoryImplTest {

    @InjectMocks
    private WordLoaderFactoryImpl wordLoaderFactory;

    @Mock
    private Map<String, WordLoader<?, ?, ?>> loaders;

    @Mock
    private List<WordLoader<? extends WordFilter, ?, ?>> wordLoaders;

    @BeforeEach
    public void setUp() {
        AllRepetitionWordCriteriaLoader allRepetitionWordCriteriaLoader = mock(AllRepetitionWordCriteriaLoader.class);
        when(allRepetitionWordCriteriaLoader.getFilter()).thenReturn(CriteriaFilterType.ALL);

        CountRepetitionWordCriteriaLoader countRepetitionWordCriteriaLoader =
                mock(CountRepetitionWordCriteriaLoader.class);
        when(countRepetitionWordCriteriaLoader.getFilter()).thenReturn(CriteriaFilterType.BY_COUNT);

        DateRepetitionWordCriteriaLoader dateRepetitionWordCriteriaLoader =
                mock(DateRepetitionWordCriteriaLoader.class);
        when(dateRepetitionWordCriteriaLoader.getFilter()).thenReturn(CriteriaFilterType.BY_DATE);

        DictionaryPageLoaderAllASC allASC = mock(DictionaryPageLoaderAllASC.class);
        when(allASC.getFilter()).thenReturn(PageFilter.BY_ADDED_AT_ASC);

        DictionaryPageLoaderAllDESC allDESC = mock(DictionaryPageLoaderAllDESC.class);
        when(allDESC.getFilter()).thenReturn(PageFilter.BY_ADDED_AT_DESC);

        DictionaryPageLoaderOnlyUseInRepetitionASC onlyUseInRepetitionASC =
                mock(DictionaryPageLoaderOnlyUseInRepetitionASC.class);
        when(onlyUseInRepetitionASC.getFilter()).thenReturn(PageFilter.ONLY_USE_IN_REPETITION_ASC);

        DictionaryPageLoaderOnlyUseInRepetitionDESC onlyUseInRepetitionDESC =
                mock(DictionaryPageLoaderOnlyUseInRepetitionDESC.class);
        when(onlyUseInRepetitionDESC.getFilter()).thenReturn(PageFilter.ONLY_USE_IN_REPETITION_DESC);

        DictionaryPageLoaderOnlyNotUseInRepetitionASC onlyNotUseInRepetitionASC =
                mock(DictionaryPageLoaderOnlyNotUseInRepetitionASC.class);
        when(onlyNotUseInRepetitionASC.getFilter()).thenReturn(PageFilter.ONLY_NOT_USE_IN_REPETITION_ASC);

        DictionaryPageLoaderOnlyNotUseInRepetitionDESC onlyNotUseInRepetitionDESC =
                mock(DictionaryPageLoaderOnlyNotUseInRepetitionDESC.class);
        when(onlyNotUseInRepetitionDESC.getFilter()).thenReturn(PageFilter.ONLY_NOT_USE_IN_REPETITION_DESC);

        List<WordLoader> wordLoaders = Arrays.asList(
                allRepetitionWordCriteriaLoader,
                countRepetitionWordCriteriaLoader,
                dateRepetitionWordCriteriaLoader,
                allASC,
                allDESC,
                onlyUseInRepetitionASC,
                onlyUseInRepetitionDESC,
                onlyNotUseInRepetitionASC,
                onlyNotUseInRepetitionDESC);

        this.loaders = wordLoaders.stream()
                .collect(Collectors.toMap(
                        loader -> ((Enum<? extends WordFilter>) loader.getFilter()).name(), loader -> loader));
        ReflectionTestUtils.setField(wordLoaderFactory, "loaders", this.loaders);
    }

    @Test
    void testGetLoader_WhenGivenAllCriteriaFilterType_ThenReturnAllRepetitionWordCriteriaLoader() {
        WordLoader loader = wordLoaderFactory.getLoader(CriteriaFilterType.ALL);
        assertThat(loader).isInstanceOf(AllRepetitionWordCriteriaLoader.class);
    }

    @Test
    void testGetLoader_WhenGivenCountCriteriaFilterType_ThenReturnCountRepetitionWordCriteriaLoader() {
        WordLoader loader = wordLoaderFactory.getLoader(CriteriaFilterType.BY_COUNT);
        assertThat(loader).isInstanceOf(CountRepetitionWordCriteriaLoader.class);
    }

    @Test
    void testGetLoader_WhenGivenDateCriteriaFilterType_ThenReturnDateRepetitionWordCriteriaLoader() {
        WordLoader loader = wordLoaderFactory.getLoader(CriteriaFilterType.BY_DATE);
        assertThat(loader).isInstanceOf(DateRepetitionWordCriteriaLoader.class);
    }

    @Test
    void testGetLoader_WhenGivenBY_ADDED_AT_ASCFilter_ThenReturnDictionaryPageLoaderAllASC() {
        WordLoader dictionaryPageLoader = wordLoaderFactory.getLoader(PageFilter.BY_ADDED_AT_ASC);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderAllASC.class);
    }

    @Test
    void testGetLoader_WhenGivenBY_ADDED_AT_DESCFilter_ThenReturnDictionaryPageLoaderAllDESC() {
        WordLoader dictionaryPageLoader = wordLoaderFactory.getLoader(PageFilter.BY_ADDED_AT_DESC);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderAllDESC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_USE_IN_REPETITION_ASCFilter_ThenReturnDictionaryPageLoaderOnlyUseInRepetitionASC() {
        WordLoader dictionaryPageLoader = wordLoaderFactory.getLoader(PageFilter.ONLY_USE_IN_REPETITION_ASC);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyUseInRepetitionASC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_USE_IN_REPETITION_DESCFilter_ThenReturnDictionaryPageLoaderOnlyUseInRepetitionDESC() {
        WordLoader dictionaryPageLoader = wordLoaderFactory.getLoader(PageFilter.ONLY_USE_IN_REPETITION_DESC);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyUseInRepetitionDESC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_NOT_USE_IN_REPETITION_ASCFilter_ThenReturnDictionaryPageLoaderOnlyNotUseInRepetitionASC() {
        WordLoader dictionaryPageLoader = wordLoaderFactory.getLoader(PageFilter.ONLY_NOT_USE_IN_REPETITION_ASC);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyNotUseInRepetitionASC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_NOT_USE_IN_REPETITION_DESCFilter_ThenReturnDictionaryPageLoaderOnlyNotUseInRepetitionDESC() {
        WordLoader dictionaryPageLoader = wordLoaderFactory.getLoader(PageFilter.ONLY_NOT_USE_IN_REPETITION_DESC);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyNotUseInRepetitionDESC.class);
    }
}
