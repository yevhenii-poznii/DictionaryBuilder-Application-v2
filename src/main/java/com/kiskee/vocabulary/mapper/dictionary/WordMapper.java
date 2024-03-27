package com.kiskee.vocabulary.mapper.dictionary;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WordMapper {

    WordDto toDto(Word word);

    @Mappings({
            @Mapping(target = "useInRepetition", constant = "true"),
            @Mapping(target = "addedAt", expression = "java(java.time.Instant.now())")
    })
    Word toEntity(WordSaveRequest saveRequest, Long dictionaryId);

    @Mappings({
            @Mapping(source = "wordTranslations", target = "wordTranslations"),
            @Mapping(target = "editedAt", expression = "java(java.time.Instant.now())"),
    })
    Word toEntity(@MappingTarget Word word, WordUpdateRequest updateRequest, List<WordTranslation> wordTranslations);

}
