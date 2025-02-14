package com.kiskee.dictionarybuilder.model.dto.repetition.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria.CountCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria.DateCriteriaFilter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "filterType", visible = true)
@JsonSubTypes(
        value = {
            @JsonSubTypes.Type(value = DefaultCriteriaFilter.class, name = "ALL"),
            @JsonSubTypes.Type(value = DateCriteriaFilter.class, name = "BY_DATE"),
            @JsonSubTypes.Type(value = CountCriteriaFilter.class, name = "BY_COUNT")
        },
        failOnRepeatedNames = true)
public interface CriteriaFilter {

    CriteriaFilterType getFilterType();

    default Object getFilterValue() {
        return null;
    }
}
