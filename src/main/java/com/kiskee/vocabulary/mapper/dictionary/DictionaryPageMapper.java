package com.kiskee.vocabulary.mapper.dictionary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryPageMapper {

    DictionaryPageResponseDto toDto(List<Word> words, int totalPages, long totalElements);

    WordTranslationDto toDto(WordTranslation wordTranslation);
}
