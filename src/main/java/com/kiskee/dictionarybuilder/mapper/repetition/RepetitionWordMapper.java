package com.kiskee.dictionarybuilder.mapper.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionDataDto;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepetitionWordMapper {

    List<WordDto> toDto(List<Word> repetitionWords);

    @Mapping(source = "repetitionData.wordHint", target = "wordHint")
    WSResponse toWSResponse(RepetitionDataDto repetitionData);

    @Mapping(source = "repetitionData.wordHint", target = "wordHint")
    WSResponse toWSResponse(RepetitionDataDto repetitionData, Long correctTranslationsCount);
}
