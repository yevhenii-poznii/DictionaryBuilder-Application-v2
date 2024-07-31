package com.kiskee.dictionarybuilder.mapper.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RepetitionWordMapper {

    List<WordDto> toDto(List<Word> repetitionWords);

    @Mappings({
        @Mapping(source = "repetitionData.currentWord.word", target = "word"),
        @Mapping(source = "repetitionData.currentWord.wordHint", target = "wordHint")
    })
    WSResponse toWSResponse(RepetitionData repetitionData);

    @Mappings({
        @Mapping(source = "repetitionData.currentWord.word", target = "word"),
        @Mapping(source = "repetitionData.currentWord.wordHint", target = "wordHint")
    })
    WSResponse toWSResponse(RepetitionData repetitionData, Long correctTranslationsCount);
}
