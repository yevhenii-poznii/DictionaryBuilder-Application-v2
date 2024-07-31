package com.kiskee.dictionarybuilder.mapper.dictionary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.WordTranslation;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WordTranslationMapper {

    List<WordTranslation> toEntities(List<WordTranslationDto> wordTranslations);
}
