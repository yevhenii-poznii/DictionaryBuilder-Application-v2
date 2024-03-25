package com.kiskee.vocabulary.mapper.dictionary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DictionaryPageMapper {

    DictionaryPageResponseDto toDto(List<Word> words, int totalPages, long totalElements);

    WordTranslationDto toDto(WordTranslation wordTranslation);

}
