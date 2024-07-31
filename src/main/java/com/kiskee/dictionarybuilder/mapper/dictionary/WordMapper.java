package com.kiskee.dictionarybuilder.mapper.dictionary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.WordTranslation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface WordMapper {

    WordDto toDto(Word word);

    Word toEntity(WordSaveRequest saveRequest, Long dictionaryId);

    @Mappings({
        @Mapping(source = "wordTranslations", target = "wordTranslations"),
        @Mapping(target = "editedAt", expression = "java(java.time.Instant.now())"),
    })
    Word toEntity(@MappingTarget Word word, WordUpdateRequest updateRequest, List<WordTranslation> wordTranslations);
}
