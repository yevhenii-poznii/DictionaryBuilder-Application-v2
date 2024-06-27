package com.kiskee.vocabulary.model.dto.repetition.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WSRequest {

    private String input;
    private Operation operation;

    public enum Operation {
        NEXT,
        SKIP
    }
}
