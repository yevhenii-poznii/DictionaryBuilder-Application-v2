package com.kiskee.dictionarybuilder.model.dto.repetition.filter;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCriteriaFilter implements CriteriaFilter {

    private CriteriaFilterType filterType;
}
