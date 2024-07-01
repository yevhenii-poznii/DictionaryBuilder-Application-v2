package com.kiskee.vocabulary.mapper.dictionary;

import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    DictionaryDetailDto toDto(Dictionary dictionary);
}
