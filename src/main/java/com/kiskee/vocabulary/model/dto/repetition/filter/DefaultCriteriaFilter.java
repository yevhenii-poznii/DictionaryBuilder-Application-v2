package com.kiskee.vocabulary.model.dto.repetition.filter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCriteriaFilter implements CriteriaFilter {

    private CriteriaFilterType filterType;

    public enum CriteriaFilterType {
        ALL,
        BY_DATE,
        BY_COUNT
    }
}

