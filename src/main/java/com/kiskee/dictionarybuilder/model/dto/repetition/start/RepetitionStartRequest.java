package com.kiskee.dictionarybuilder.model.dto.repetition.start;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.CriteriaFilter;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = RepetitionStartFilterRequest.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RepetitionStartFilterRequest.class),
    @JsonSubTypes.Type(value = SharingRepetitionStartFilterRequest.class)
})
public interface RepetitionStartRequest extends RepetitionRequest {

    RepetitionStartFilterRequest.RepetitionFilter getRepetitionFilter();

    CriteriaFilter getCriteriaFilter();

    RepetitionType getRepetitionType();

    Boolean getReversed();
}
