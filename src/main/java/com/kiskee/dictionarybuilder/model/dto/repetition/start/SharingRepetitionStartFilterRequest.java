package com.kiskee.dictionarybuilder.model.dto.repetition.start;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharingRepetitionStartFilterRequest extends RepetitionStartFilterRequest
        implements RepetitionStartRequest {

    @Nullable
    private String sharingToken;
}
