package com.kiskee.vocabulary.mapper.dictionary;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WordTranslationMapper {

    List<WordTranslation> toEntities(List<WordTranslationDto> wordTranslations);

}
