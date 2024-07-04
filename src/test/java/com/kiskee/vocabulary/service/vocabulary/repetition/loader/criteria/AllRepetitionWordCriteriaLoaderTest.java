package com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria;

import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.repository.repetition.RepetitionWordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AllRepetitionWordCriteriaLoaderTest {

    @InjectMocks
    private AllRepetitionWordCriteriaLoader allRepetitionWordCriteriaLoader;

    @Mock
    private RepetitionWordRepository repository;
    @Mock
    private RepetitionWordMapper mapper;

    @Test
    void test() {

    }
}
