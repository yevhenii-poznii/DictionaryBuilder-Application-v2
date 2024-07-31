package com.kiskee.dictionarybuilder.model.dto.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.filter.CriteriaFilter;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionStartFilterRequest {

    @NotNull
    private RepetitionFilter repetitionFilter;

    @NotNull
    private CriteriaFilter criteriaFilter;

    public enum RepetitionFilter {
        REPETITION_ONLY,
        NOT_REPETITION_ONLY,
        ALL
    }
}
