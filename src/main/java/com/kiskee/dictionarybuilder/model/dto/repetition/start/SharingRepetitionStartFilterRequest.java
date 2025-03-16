package com.kiskee.dictionarybuilder.model.dto.repetition.start;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.CriteriaFilter;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharingRepetitionStartFilterRequest extends RepetitionStartFilterRequest
        implements RepetitionStartRequest {

    @Nullable
    private String sharingToken;

    public SharingRepetitionStartFilterRequest(
            @NotNull RepetitionFilter repetitionFilter,
            @NotNull CriteriaFilter criteriaFilter,
            @NotNull RepetitionType repetitionType,
            @NotNull Boolean reversed,
            @Nullable String sharingToken) {
        super(repetitionFilter, criteriaFilter, repetitionType, reversed);
        this.sharingToken = sharingToken;
    }
}
