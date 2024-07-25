package com.kiskee.vocabulary.mapper.repetition;

import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.entity.redis.repetition.RepetitionData;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
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
