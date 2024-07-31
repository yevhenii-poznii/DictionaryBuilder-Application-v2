package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.AllRepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.CountRepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.DateRepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
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
public class RepetitionWordLoaderFactoryImplTest {

    @InjectMocks
    private RepetitionWordLoaderFactoryImpl repetitionWordLoaderFactory;

    @Mock
    private Map<String, RepetitionWordCriteriaLoader> loaders;

    @Mock
    private List<RepetitionWordCriteriaLoader> repetitionWordCriteriaLoaders;

    @BeforeEach
    public void setUp() {
        AllRepetitionWordCriteriaLoader allRepetitionWordCriteriaLoader = mock(AllRepetitionWordCriteriaLoader.class);
        when(allRepetitionWordCriteriaLoader.getCriteriaFilter())
                .thenReturn(DefaultCriteriaFilter.CriteriaFilterType.ALL);

        CountRepetitionWordCriteriaLoader countRepetitionWordCriteriaLoader =
                mock(CountRepetitionWordCriteriaLoader.class);
        when(countRepetitionWordCriteriaLoader.getCriteriaFilter())
                .thenReturn(DefaultCriteriaFilter.CriteriaFilterType.BY_COUNT);

        DateRepetitionWordCriteriaLoader dateRepetitionWordCriteriaLoader =
                mock(DateRepetitionWordCriteriaLoader.class);
        when(dateRepetitionWordCriteriaLoader.getCriteriaFilter())
                .thenReturn(DefaultCriteriaFilter.CriteriaFilterType.BY_DATE);

        this.repetitionWordCriteriaLoaders = Arrays.asList(
                allRepetitionWordCriteriaLoader, countRepetitionWordCriteriaLoader, dateRepetitionWordCriteriaLoader);

        this.loaders = repetitionWordCriteriaLoaders.stream()
                .collect(Collectors.toMap(loader -> loader.getCriteriaFilter().name(), loader -> loader));
        ReflectionTestUtils.setField(repetitionWordLoaderFactory, "loaders", this.loaders);
    }

    @Test
    void testGetLoader_WhenGivenAllCriteriaFilterType_ThenReturnAllRepetitionWordCriteriaLoader() {
        DefaultCriteriaFilter.CriteriaFilterType criteriaFilterType = DefaultCriteriaFilter.CriteriaFilterType.ALL;

        RepetitionWordCriteriaLoader loader = repetitionWordLoaderFactory.getLoader(criteriaFilterType);
        assertThat(loader).isInstanceOf(AllRepetitionWordCriteriaLoader.class);
    }

    @Test
    void testGetLoader_WhenGivenCountCriteriaFilterType_ThenReturnCountRepetitionWordCriteriaLoader() {
        DefaultCriteriaFilter.CriteriaFilterType criteriaFilterType = DefaultCriteriaFilter.CriteriaFilterType.BY_COUNT;

        RepetitionWordCriteriaLoader loader = repetitionWordLoaderFactory.getLoader(criteriaFilterType);
        assertThat(loader).isInstanceOf(CountRepetitionWordCriteriaLoader.class);
    }

    @Test
    void testGetLoader_WhenGivenDateCriteriaFilterType_ThenReturnDateRepetitionWordCriteriaLoader() {
        DefaultCriteriaFilter.CriteriaFilterType criteriaFilterType = DefaultCriteriaFilter.CriteriaFilterType.BY_DATE;

        RepetitionWordCriteriaLoader loader = repetitionWordLoaderFactory.getLoader(criteriaFilterType);
        assertThat(loader).isInstanceOf(DateRepetitionWordCriteriaLoader.class);
    }
}
