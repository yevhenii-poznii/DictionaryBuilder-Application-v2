package com.kiskee.vocabulary.mapper.repetition;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RepetitionWordMapper {

    List<WordDto> toDto(List<Word> repetitionWords);
}
