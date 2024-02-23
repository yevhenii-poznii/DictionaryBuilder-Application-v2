package com.kiskee.vocabulary.service.vocabulary;

import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryRepository;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DictionaryServiceTest {

    @InjectMocks
    private DictionaryService vocabularyService;
    @Mock
    private DictionaryRepository dictionaryRepository;
    @Captor
    private ArgumentCaptor<Dictionary> dictionaryArgumentCaptor;

    @Test
    void testInitDefault_WhenDictionaryNameIsGiven_ThenCreateEmptyDictionary() {
        String dictionaryName = "Default Dictionary";

        vocabularyService.addDictionary(dictionaryName);

        verify(dictionaryRepository).save(dictionaryArgumentCaptor.capture());

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getDictionaryName()).isEqualTo(dictionaryName);
        assertThat(actual.getWords()).isEqualTo(Collections.emptyList());
    }

}
