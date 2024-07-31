package com.kiskee.dictionarybuilder.mapper.dictionary;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    DictionaryDetailDto toDto(Dictionary dictionary);
}
