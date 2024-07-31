package com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page.impl.DictionaryPageLoaderFactoryImpl;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderAllASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderOnlyNotUseInRepetitionASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.asc.DictionaryPageLoaderOnlyUseInRepetitionASC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderAllDESC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderOnlyNotUseInRepetitionDESC;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.impl.desc.DictionaryPageLoaderOnlyUseInRepetitionDESC;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DictionaryPageLoaderFactoryImplTest {

    @Mock
    private DictionaryPageLoaderAllASC allASC;

    @Mock
    private DictionaryPageLoaderAllDESC allDESC;

    @Mock
    private DictionaryPageLoaderOnlyUseInRepetitionASC onlyUseInRepetitionASC;

    @Mock
    private DictionaryPageLoaderOnlyUseInRepetitionDESC onlyUseInRepetitionDESC;

    @Mock
    private DictionaryPageLoaderOnlyNotUseInRepetitionASC onlyNotUseInRepetitionASC;

    @Mock
    private DictionaryPageLoaderOnlyNotUseInRepetitionDESC onlyNotUseInRepetitionDESC;

    private DictionaryPageLoaderFactoryImpl dictionaryPageLoaderFactory;

    @BeforeEach
    public void setUp() {
        when(allASC.getPageFilter()).thenReturn(PageFilter.BY_ADDED_AT_ASC);
        when(allDESC.getPageFilter()).thenReturn(PageFilter.BY_ADDED_AT_DESC);
        when(onlyUseInRepetitionASC.getPageFilter()).thenReturn(PageFilter.ONLY_USE_IN_REPETITION_ASC);
        when(onlyUseInRepetitionDESC.getPageFilter()).thenReturn(PageFilter.ONLY_USE_IN_REPETITION_DESC);
        when(onlyNotUseInRepetitionASC.getPageFilter()).thenReturn(PageFilter.ONLY_NOT_USE_IN_REPETITION_ASC);
        when(onlyNotUseInRepetitionDESC.getPageFilter()).thenReturn(PageFilter.ONLY_NOT_USE_IN_REPETITION_DESC);

        dictionaryPageLoaderFactory = new DictionaryPageLoaderFactoryImpl(Arrays.asList(
                allASC,
                allDESC,
                onlyUseInRepetitionASC,
                onlyUseInRepetitionDESC,
                onlyNotUseInRepetitionASC,
                onlyNotUseInRepetitionDESC));
    }

    @Test
    void testGetLoader_WhenGivenBY_ADDED_AT_ASCFilter_ThenReturnDictionaryPageLoaderAllASC() {
        PageFilter filter = PageFilter.BY_ADDED_AT_ASC;

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(filter);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderAllASC.class);
    }

    @Test
    void testGetLoader_WhenGivenBY_ADDED_AT_DESCFilter_ThenReturnDictionaryPageLoaderAllDESC() {
        PageFilter filter = PageFilter.BY_ADDED_AT_DESC;

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(filter);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderAllDESC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_USE_IN_REPETITION_ASCFilter_ThenReturnDictionaryPageLoaderOnlyUseInRepetitionASC() {
        PageFilter filter = PageFilter.ONLY_USE_IN_REPETITION_ASC;

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(filter);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyUseInRepetitionASC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_USE_IN_REPETITION_DESCFilter_ThenReturnDictionaryPageLoaderOnlyUseInRepetitionDESC() {
        PageFilter filter = PageFilter.ONLY_USE_IN_REPETITION_DESC;

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(filter);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyUseInRepetitionDESC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_NOT_USE_IN_REPETITION_ASCFilter_ThenReturnDictionaryPageLoaderOnlyNotUseInRepetitionASC() {
        PageFilter filter = PageFilter.ONLY_NOT_USE_IN_REPETITION_ASC;

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(filter);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyNotUseInRepetitionASC.class);
    }

    @Test
    void
            testGetLoader_WhenGivenONLY_NOT_USE_IN_REPETITION_DESCFilter_ThenReturnDictionaryPageLoaderOnlyNotUseInRepetitionDESC() {
        PageFilter filter = PageFilter.ONLY_NOT_USE_IN_REPETITION_DESC;

        DictionaryPageLoader dictionaryPageLoader = dictionaryPageLoaderFactory.getLoader(filter);

        assertThat(dictionaryPageLoader).isInstanceOf(DictionaryPageLoaderOnlyNotUseInRepetitionDESC.class);
    }
}
