package com.kiskee.dictionarybuilder.model.dto.repetition.message;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WSRequest implements RepetitionRequest {

    private String input;
    private Operation operation;

    public enum Operation {
        START,
        CHECK,
        SKIP
    }
}
