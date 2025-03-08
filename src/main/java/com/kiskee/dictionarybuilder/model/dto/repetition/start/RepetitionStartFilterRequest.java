package com.kiskee.dictionarybuilder.model.dto.repetition.start;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.CriteriaFilter;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionStartFilterRequest implements RepetitionStartRequest {

    @NotNull
    private RepetitionFilter repetitionFilter;

    @NotNull
    private CriteriaFilter criteriaFilter;

    @NotNull
    private RepetitionType repetitionType;

    @NotNull
    private Boolean reversed;

    public enum RepetitionFilter {
        REPETITION_ONLY,
        NOT_REPETITION_ONLY,
        ALL
    }
}
